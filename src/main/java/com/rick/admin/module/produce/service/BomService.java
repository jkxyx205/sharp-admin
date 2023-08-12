package com.rick.admin.module.produce.service;

import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/7/1 10:52
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class BomService {

    MaterialDAO materialDAO;

    DictService dictService;

    MaterialService materialService;

    @Resource
    private EntityCodeDAO<BomTemplate, Long> bomTemplateDAO;

    public BomTemplate getBomTemplateMaterialId(Long materialId, Map<Long, ProduceOrder.Item.Detail> valueMapping) {
        return getBomTemplateMaterialId(materialId, valueMapping, false);
    }

    public BomTemplate getBomTemplateMaterialId(Long materialId, Map<Long, ProduceOrder.Item.Detail> valueMapping, Boolean resetValueAdditionalFields) {
        Optional<Long> bomTemplateOptional = materialDAO.selectSingleValueById(materialId, "bom_template_id", Long.class);
        if (!bomTemplateOptional.isPresent()) {
            throw new BizException(500, "没有找到 bom 模版");
        }

        BomTemplate bomTemplate = bomTemplateDAO.selectById(bomTemplateOptional.get()).get();

        Set<Long> materialIdSet = bomTemplate.getComponentList().stream().flatMap(component -> component.getComponentDetailList().stream())
                .filter(d -> d.getType() == BomTemplate.TypeEnum.MATERIAL).map(BomTemplate.ComponentDetail::getTypeInstanceId).collect(Collectors.toSet());

        Map<Long, Material> idMaterialMap = null;

        if (CollectionUtils.isNotEmpty(materialIdSet)) {
            idMaterialMap = materialDAO.selectByParamsWithoutCascade(Params.builder(1).pv("id", materialIdSet).build(), "id IN (:id)")
                    .stream().collect(Collectors.toMap(Material::getId, material -> material));
        }

        for (BomTemplate.Component component : bomTemplate.getComponentList()) {
            component.setUnitText(dictService.getDictByTypeAndName("unit", component.getUnit()).get().getLabel());
            for (BomTemplate.ComponentDetail componentDetail : component.getComponentDetailList()) {
                componentDetail.setOptions(dictService.getDictByType("COLOR").stream().map(Dict::getLabel).collect(Collectors.toList()));
                ProduceOrder.Item.Detail value = valueMapping.get(componentDetail.getId());
                if (Objects.nonNull(value)) {
                    // 实例
                    if (Objects.equals(Boolean.TRUE, resetValueAdditionalFields)) {
                        BaseEntityUtils.resetAdditionalFields(value);
                    }

                    componentDetail.setValue(value);
                } else if (componentDetail.getType() == BomTemplate.TypeEnum.CATEGORY) {
                    componentDetail.setValue(new ProduceOrder.Item.Detail());
                } else if (componentDetail.getType() == BomTemplate.TypeEnum.MATERIAL) {
                    Material material = idMaterialMap.get(componentDetail.getTypeInstanceId());

                    value = ProduceOrder.Item.Detail.builder()
                            .materialId(material.getId())
                            .quantity(componentDetail.getQuantity())
                            .unit(material.getBaseUnit())
                            .remark("")
                            .componentDetailId(componentDetail.getId())
                            .build();
                    componentDetail.setValue(value);

                    if (material.isBomMaterial()) {
                        componentDetail.setBomTemplate(getBomTemplateMaterialId(material.getId(), valueMapping, resetValueAdditionalFields));
                    }
                }
            }
        }

        Set<ProduceOrder.Item.Detail> values = bomTemplate.getComponentList().stream().flatMap(component -> component.getComponentDetailList().stream())
                .filter(componentDetail -> Objects.nonNull(componentDetail.getValue().getMaterialId()))
                .map(BomTemplate.ComponentDetail::getValue)
                .collect(Collectors.toSet());

        materialService.fillMaterialDescription(values);
        return bomTemplate;
    }

}
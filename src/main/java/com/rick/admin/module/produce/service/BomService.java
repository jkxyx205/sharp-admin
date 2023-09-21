package com.rick.admin.module.produce.service;

import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    Map<Long, BomTemplate> materialIdBomTemplateMap = new HashMap<>();

    Map<Long, Material> idMaterialMap = new HashMap<>();

    SharpService sharpService;

    @Resource
    private EntityCodeDAO<BomTemplate, Long> bomTemplateDAO;

    @PostConstruct
    public void init() {
        String sql = "select id, bom_template_id from mm_material where `bom_template_id` is not null";
        List<Map<String, Object>> list = sharpService.query(sql, null);

        Map<Long, BomTemplate> bomTemplateIdMap = bomTemplateDAO.selectByIdsAsMap(list.stream().map(map -> map.get("bom_template_id")).collect(Collectors.toSet()));

        for (Map<String, Object> row : list) {
            materialIdBomTemplateMap.put((Long) row.get("id"), bomTemplateIdMap.get(row.get("bom_template_id")));
        }

        Set<Long> materialIdSet = bomTemplateIdMap.values().stream().flatMap(bomTemplate -> bomTemplate.getComponentList().stream()).flatMap(component -> component.getComponentDetailList().stream())
                .filter(d -> d.getType() == BomTemplate.TypeEnum.MATERIAL).map(BomTemplate.ComponentDetail::getTypeInstanceId).collect(Collectors.toSet());


        if (CollectionUtils.isNotEmpty(materialIdSet)) {
            idMaterialMap.putAll(materialDAO.selectByParamsWithoutCascade(Params.builder(1).pv("id", materialIdSet).build(), "id IN (:id)")
                    .stream().collect(Collectors.toMap(Material::getId, material -> material)));
        }

    }

    public BomTemplate getBomTemplateMaterialId(Long materialId, Map<Long, ProduceOrder.Item.Detail> valueMapping) {
        return getBomTemplateMaterialId(materialId, valueMapping, false);
    }

    public BomTemplate getBomTemplateMaterialId(Long materialId, Map<Long, ProduceOrder.Item.Detail> valueMapping, Boolean resetValueAdditionalFields) {
        BomTemplate bomTemplate = SerializationUtils.clone(materialIdBomTemplateMap.get(materialId));
        if (Objects.isNull(bomTemplate)) {
            throw new BizException(500, "没有找到 bom 模版");
        }

        for (BomTemplate.Component component : bomTemplate.getComponentList()) {
            if (StringUtils.isNotBlank(component.getUnit())) {
                component.setUnitText(dictService.getDictByTypeAndName("unit", component.getUnit()).get().getLabel());
            }

            for (BomTemplate.ComponentDetail componentDetail : component.getComponentDetailList()) {
                ProduceOrder.Item.Detail value = valueMapping.get(componentDetail.getId());
                if (Objects.nonNull(value)) {
                    // 实例
                    if (Objects.equals(Boolean.TRUE, resetValueAdditionalFields)) {
                        BaseEntityUtils.resetAdditionalFields(value);
                    }

                    componentDetail.setValue(value);
                    componentDetail.setClassificationList(value.getClassificationList());
                    componentDetail.setQuantity(value.getQuantity());
                } else if (componentDetail.getType() == BomTemplate.TypeEnum.CATEGORY) {
                    value = ProduceOrder.Item.Detail.builder()
                            .quantity(componentDetail.getQuantity())
                            .remark("")
                            .componentDetailId(componentDetail.getId())
                            .build();
                    componentDetail.setValue(value);
                } else if (componentDetail.getType() == BomTemplate.TypeEnum.MATERIAL) {
                    Material material = idMaterialMap.get(componentDetail.getTypeInstanceId());

                    value = ProduceOrder.Item.Detail.builder()
                            .materialId(material.getId())
                            .materialCode(material.getCode())
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

        Set<BomTemplate.ComponentDetail> componentDetails = bomTemplate.getComponentList().stream().flatMap(component -> component.getComponentDetailList().stream())
                .filter(componentDetail -> Objects.nonNull(componentDetail.getValue().getMaterialId()))
                .collect(Collectors.toSet());

        materialService.fillMaterialDescription(Stream.concat(values.stream(), componentDetails.stream()).collect(Collectors.toSet()));
        return bomTemplate;
    }

}
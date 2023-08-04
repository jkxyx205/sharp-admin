package com.rick.admin.module.produce.service;

import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.entity.Bom;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
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

    EntityDAO<Bom, Long> bomDAO;

    MaterialDAO materialDAO;

    DictService dictService;

    MaterialService materialService;

    @Resource
    private EntityCodeDAO<BomTemplate, Long> bomTemplateDAO;

    @Transactional(rollbackFor = Exception.class)
    public Bom saveOrUpdate(@Valid Bom bom) {
        bomDAO.insertOrUpdate(bom);

        // 更新物料价格
//        List<Object[]> paramList = bom.getItemList().stream().filter(item -> Objects.nonNull(item.getUnitPrice()))
//                .map(item -> new Object[]{item.getUnitPrice(), item.getMaterialId()}).collect(Collectors.toList());
//        materialDAO.updatePrice(paramList);

        return bom;
    }

    public BomTemplate getBomTemplateMaterialId(Long materialId) {
        Optional<Long> bomTemplateOptional = materialDAO.selectSingleValueById(materialId, "bom_template_id", Long.class);
        if (!bomTemplateOptional.isPresent()) {
            throw new BizException(500, "没有找到 bom 模版");
        }

        Bom bom = findByMaterialId(materialId);

        bom.setBomTemplateId(bomTemplateOptional.get());
        Map<Long, Bom.Item> componentDetailIdBomItemMap = bom.getItemList().stream().collect(Collectors.toMap(bomItem -> bomItem.getComponentDetailId(), bomItem -> bomItem));

        BomTemplate bomTemplate = bomTemplateDAO.selectById(bom.getBomTemplateId()).get();

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
                Bom.Item item = componentDetailIdBomItemMap.get(componentDetail.getId());
                if (componentDetail.getType() == BomTemplate.TypeEnum.CATEGORY) {
                    componentDetail.setBomItem(ObjectUtils.defaultIfNull(item, new Bom.Item()));
                } else if (componentDetail.getType() == BomTemplate.TypeEnum.MATERIAL) {
                    if (Objects.isNull(item)) {
//                        Material material = materialDAO.selectByParamsWithoutCascade(Material.builder().id(componentDetail.getTypeInstanceId()).build()).get(0);
                        Material material = idMaterialMap.get(componentDetail.getTypeInstanceId());
                        item = Bom.Item.builder()
                                .materialId(material.getId())
                                .quantity(componentDetail.getQuantity())
                                .unit(material.getBaseUnit())
                                .remark("")
                                .componentDetailId(componentDetail.getId())
//                            .bomId()
                                .build();

                        fillMaterialExtraData(item, material);
                        materialService.fillMaterialDescription(bom.getItemList());
                    }
                    componentDetail.setBomItem(item);
                }
            }
        }

        bomTemplate.setBom(bom);
        return bomTemplate;
    }

    public Bom findByMaterialId(Long materialId) {
        List<Bom> bomList =  bomDAO.selectByParams(Bom.builder().materialId(materialId).build());
        if (bomList.size() == 0) {
            Bom bom = new Bom();
            bom.setItemList(Collections.EMPTY_LIST);
            return bom;
        }

        Bom bom = bomList.get(0);
        materialService.fillMaterialDescription(bom.getItemList());
        return bom;
    }

    private void fillMaterialExtraData(Bom.Item item, Material material) {
        item.setMaterialCode(material.getCode());
        item.setMaterialText(material.getName() + " " + material.getSpecificationText());
        item.setUnitText(dictService.getDictByTypeAndName("unit", item.getUnit()).get().getLabel());
        item.setUnitPrice(material.getStandardPrice());
    }

}
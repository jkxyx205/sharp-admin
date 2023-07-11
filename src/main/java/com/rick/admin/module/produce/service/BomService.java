package com.rick.admin.module.produce.service;

import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.produce.entity.Bom;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public Bom saveOrUpdate(Bom bom) {
        bomDAO.insertOrUpdate(bom);

        // 更新物料价格
        List<Object[]> paramList = bom.getItemList().stream().filter(item -> Objects.nonNull(item.getUnitPrice()))
                .map(item -> new Object[]{item.getUnitPrice(), item.getMaterialId()}).collect(Collectors.toList());
        materialDAO.updatePrice(paramList);

        return bom;
    }

    public Bom findByMaterialId(Long materialId) {
        List<Bom> bomList =  bomDAO.selectByParams(Bom.builder().materialId(materialId).build());
        if (bomList.size() == 0) {
            return new Bom();
        }

        Bom bom = bomList.get(0);
        Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(bom.getItemList().stream().map(Bom.Item::getMaterialId).collect(Collectors.toSet()));

        for (Bom.Item item : bom.getItemList()) {
            Material material = idMaterialMap.get(item.getMaterialId());
            item.setMaterialCode(material.getCode());
            item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
            item.setUnitText(dictService.getDictByTypeAndName("unit", item.getUnit()).get().getLabel());
            item.setUnitPrice(material.getStandardPrice());
        }

        return bom;
    }

}
package com.rick.admin.module.material.service;

import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/1 23:46
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class MaterialService {

    MaterialDAO materialDAO;

    final DictService dictService;

    public void fillMaterialDescription(List<? extends MaterialDescription> materialDescriptionList) {
        if (CollectionUtils.isNotEmpty(materialDescriptionList)) {
            consumeMaterialDescription(materialDescriptionList.stream().map(MaterialDescription::getMaterialId).collect(Collectors.toSet()), (idMaterialMap, dictService) -> {
                for (MaterialDescription item : materialDescriptionList) {
                    Material material = idMaterialMap.get(item.getMaterialId());
                    if (material == null) {
                        continue;
                    }

                    item.setMaterialCode(material.getCode());
                    item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
                    item.setUnit(material.getBaseUnit());
                    item.setUnitText(dictService.getDictByTypeAndName("unit", material.getBaseUnit()).get().getLabel());
                    item.setMaterialCategoryId(material.getCategoryId());
                }
            });
        }
    }

    public void consumeMaterialDescription(Collection<Long> materialIds, BiConsumer<Map<Long, Material>, DictService> consumer) {
        if (CollectionUtils.isEmpty(materialIds)) {
            return;
        }

        Map<Long, Material> idMaterialMap = materialDAO.selectByParamsWithoutCascade(Params.builder(1).pv("id", materialIds).build(), "id IN (:id)")
                .stream().collect(Collectors.toMap(Material::getId, material -> material));
        if (MapUtils.isNotEmpty(idMaterialMap)) {
            consumer.accept(idMaterialMap, dictService);
        }

    }
}
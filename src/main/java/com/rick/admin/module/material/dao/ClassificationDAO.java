package com.rick.admin.module.material.dao;

import com.rick.admin.module.material.entity.Classification;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.db.service.support.Params;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/4 11:23
 */
@Repository("materialClassificationDAO")
public class ClassificationDAO extends EntityDAOImpl<Classification, Long> {

    /**
     * 根据物料id获取所有的特征值
     * @param materialId
     * @return
     */
    public List<com.rick.admin.module.core.entity.Classification> findClassificationByMaterialId(Long materialId) {
        return selectByParams(Params.builder(1).pv("materialId", materialId).build(),
                "material_id = :materialId").stream().map(Classification::getClassification).collect(Collectors.toList());
    }

    public Map<Long, List<com.rick.admin.module.core.entity.Classification>> findClassificationByMaterialIds(Collection<Long> materialIds) {
        return selectByParams(Params.builder(1).pv("materialIds", materialIds).build(),
                "material_id IN (:materialIds)").stream()
                .collect(Collectors.groupingBy(Classification::getMaterialId, Collectors.mapping(Classification::getClassification, Collectors.toList())));
    }
}
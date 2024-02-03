package com.rick.admin.module.material.service;

import com.google.common.collect.Lists;
import com.rick.admin.module.core.entity.Classification;
import com.rick.admin.module.material.dao.ClassificationDAO;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/22 16:59
 */
@Service("materialClassificationService")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class ClassificationService {

    EntityCodeDAO<Classification, Long> classificationDAO;

    ClassificationDAO materialClassificationDAO;

    public void batchAssignClassification(String classificationCode, Collection<Long> materialIds) {
        if (StringUtils.isBlank(classificationCode) || CollectionUtils.isEmpty(materialIds)) {
            return;
        }

        Classification classification =  classificationDAO.selectByCode(classificationCode).get();
        List<com.rick.admin.module.material.entity.Classification> materialClassificationList = Lists.newArrayListWithExpectedSize(materialIds.size());

        for (Long materialId : materialIds) {
            materialClassificationList.add(com.rick.admin.module.material.entity.Classification.builder()
                    .classificationCode(classificationCode)
                    .classification(classification)
                    .materialId(materialId)
                    .build());

        }

        materialClassificationDAO.insert(materialClassificationList);
    }

    /**
     * 获取物料的特征值
     * @return
     */
    public Map<Long, List<com.rick.admin.module.material.entity.Classification>> findAllAsMap() {
        return materialClassificationDAO.selectAll().stream().collect(Collectors.groupingBy(classification -> classification.getMaterialId()));
    }

}
package com.rick.admin.module.inventory.service;

import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.entity.MaterialProfile;
import com.rick.db.plugin.dao.core.EntityDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/9/22 12:15
 */
@SpringBootTest
public class BatchCodeUpdateTest {
    
    @Resource
    EntityDAO<MaterialProfile, Long> materialProfileDAO;

    @Resource
    JdbcTemplate jdbcTemplate;
    
    @Test
    public void testUpdate() {
        List<MaterialProfile> materialProfiles = materialProfileDAO.selectByParams("category=BATCH");

        for (MaterialProfile materialProfile : materialProfiles) {
            List<CharacteristicValue> characteristicValueList = materialProfile.getCharacteristicValueList();

            List<Classification> classificationList;
            if (CollectionUtils.isEmpty(characteristicValueList)) {
                classificationList = Collections.emptyList();
            } else {
                Map<String, List<CharacteristicValue>> collect = characteristicValueList.stream().collect(Collectors.groupingBy(CharacteristicValue::getClassificationCode));
                classificationList = new ArrayList<>();

                for (Map.Entry<String, List<CharacteristicValue>> stringListEntry : collect.entrySet()) {
                    classificationList.add(Classification.builder()
                                    .classificationCode(stringListEntry.getKey())
                                    .characteristicValueList(stringListEntry.getValue())
                            .build());
                }
            }

//            String batchCode = BatchSupport.characteristicToCode(classificationList);
//            Long profileId = materialProfile.getId();

//            jdbcTemplate.update("update mm_profile set batch_code = ? where id = ?", new Object[]{batchCode, profileId});
//            jdbcTemplate.update("update mm_batch set code = ? where profile_id = ?", new Object[]{batchCode, profileId});
//            jdbcTemplate.update("update inv_stock set batch_code = ? where batch_id = ? AND batch_id is not null", new Object[]{batchCode, materialProfile.getBatchId()});
//
//            jdbcTemplate.update("update inv_document_item set batch_code = ? where batch_id = ? AND batch_id is not null", new Object[]{batchCode, materialProfile.getBatchId()});
//            jdbcTemplate.update("update produce_order_item set batch_code = ? where batch_id = ? AND batch_id is not null", new Object[]{batchCode, materialProfile.getBatchId()});
//            jdbcTemplate.update("update produce_order_item_detail set batch_code = ? where batch_id = ? AND batch_id is not null", new Object[]{batchCode, materialProfile.getBatchId()});
//            jdbcTemplate.update("update pur_purchase_order_item set batch_code = ? where batch_id = ? AND batch_id is not null", new Object[]{batchCode, materialProfile.getBatchId()});
        }

    }
}

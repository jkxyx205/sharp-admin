package com.rick.admin.module.material.service;

import com.rick.report.core.entity.Report;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/6/26 10:48
 */
@UtilityClass
public class MaterialKeywordsHelper {

    public static void keywordsConditionSQLSet(Report report, Map<String, Object> requestMap) {
        String keywords = (String) requestMap.get("keywords");
        if (StringUtils.isNotBlank(keywords)) {
            String[] keywordArray = keywords.split("\\s+");

            String condition = "";
            for (int i = 0; i < keywordArray.length; i++) {
                String paramName = "keyword" + i;
                condition += " AND (mm_material.code like :"+paramName+" or mm_material.name like :"+paramName+" or specification like :"+paramName+" " +
                        "OR (batch_id in (select mm_batch.id from mm_batch, mm_profile, mm_characteristic_value where\n" +
                        " mm_batch.profile_id = mm_profile.id AND mm_characteristic_value.reference_id = mm_profile.id AND mm_characteristic_value.value like :"+paramName+")))";
                requestMap.put(paramName, keywordArray[i]);
            }

            String querySql = report.getQuerySql();
            report.setQuerySql(querySql + condition);
        }
    }
}

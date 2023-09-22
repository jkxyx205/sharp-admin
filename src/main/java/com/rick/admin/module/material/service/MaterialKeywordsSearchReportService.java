package com.rick.admin.module.material.service;

import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/8/28 13:39
 */
@Component
public class MaterialKeywordsSearchReportService implements ReportAdvice {

    @Override
    public void beforeQuery(Report report, Map<String, Object> requestMap) {
        String keywords = (String) requestMap.get("keywords");
        if (StringUtils.isNotBlank(keywords)) {
            String[] keywordArray = keywords.split("\\s+");

            String condition = "";
            for (int i = 0; i < keywordArray.length; i++) {
                String paramName = "keyword" + i;
                condition += " AND (mm_material.code like :"+paramName+" or mm_material.name like :"+paramName+" or specification like :"+paramName+")";
                requestMap.put(paramName, keywordArray[i]);
            }

            String querySql = report.getQuerySql();
            report.setQuerySql(querySql + condition);
        }
    }
}

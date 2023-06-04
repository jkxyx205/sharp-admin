package com.rick.admin.module.material.service;

import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/4 08:50
 */
@Component
public class MaterialReportAdvice implements ReportAdvice {

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            row.put("stock_quantity", RandomStringUtils.randomNumeric(4));
        }
    }
}

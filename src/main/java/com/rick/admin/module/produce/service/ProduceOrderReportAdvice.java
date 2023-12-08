package com.rick.admin.module.produce.service;

import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/11/28 15:40
 */
@Component
@RequiredArgsConstructor
public class ProduceOrderReportAdvice implements ReportAdvice {
    @Override
    public void beforeQuery(Report report, Map<String, Object> requestMap) {
        report.getAdditionalInfo().put("js", "$('#partner_id').multipleSelect({\n" +
                "    filter: true\n" +
                "})");
    }
}

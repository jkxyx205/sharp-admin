package com.rick.admin.module.produce.service;

import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/7/12 02:55
 */
@Component
public class ProduceOrderStockReportAdvice implements ReportAdvice {

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            row.put("diffQuantity",
                    ((BigDecimal)row.get("stock_quantity")).add((BigDecimal)row.get("open_quantity")).subtract((BigDecimal)row.get("quantity")));
        }

        //report.getAdditionalInfo().put("css", "div {color: red;}");
        report.getAdditionalInfo().put("js", "$('table tr td:nth-child(9) span:contains(-)').css('color', '#ffffff').parents('tr').css('background', 'rgb(231, 116, 112)').css('color', '#ffffff')");
    }
}

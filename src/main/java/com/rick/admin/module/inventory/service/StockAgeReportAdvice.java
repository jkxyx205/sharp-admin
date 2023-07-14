package com.rick.admin.module.inventory.service;

import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/4 08:50
 */
@Component
@RequiredArgsConstructor
public class StockAgeReportAdvice implements ReportAdvice {

    private final StockAgeService stockAgeService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            row.putAll(stockAgeService.getAge((Long) row.get("id"), ((BigDecimal)row.get("stock_quantity")).longValue()));
        }
    }
}

package com.rick.admin.module.inventory.service;

import com.rick.admin.module.core.service.CategoryService;
import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/4 08:50
 */
@Component
@RequiredArgsConstructor
public class StockWarningReportAdvice implements ReportAdvice {

    private final CategoryService categoryService;

    private final StockDAO stockDAO;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            row.put("category_path", categoryService.getPathById((Long) row.get("category_id")));
        }

    }
}

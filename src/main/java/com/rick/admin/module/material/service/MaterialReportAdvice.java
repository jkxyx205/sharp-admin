package com.rick.admin.module.material.service;

import com.rick.admin.module.core.service.CategoryService;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/6/4 08:50
 */
@Component
@RequiredArgsConstructor
public class MaterialReportAdvice implements ReportAdvice {

    private final CategoryService categoryService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            BigDecimal standardPrice = (BigDecimal) row.get("standard_price");

            float stockQuantity = RandomUtils.nextFloat(0, 10000);

            row.put("stock_quantity", stockQuantity);
            if (Objects.nonNull(standardPrice)) {
                row.put("stock_quantity_standard_price", BigDecimal.valueOf(stockQuantity).multiply(standardPrice));
            }
            row.put("category_path", categoryService.getPathById((Long) row.get("category_id")));
        }
    }
}

package com.rick.admin.module.material.service;

import com.rick.admin.module.core.service.CategoryService;
import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.report.core.entity.Report;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/4 08:50
 */
@Component
@RequiredArgsConstructor
public class MaterialReportAdvice extends MaterialKeywordsSearchReportService {

    private final CategoryService categoryService;

    private final StockDAO stockDAO;

    private final MaterialProfileService materialProfileService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
//        Map<Long, BigDecimal> materialIdQuantityMap = stockDAO.getStockQuantityByMaterialId(rows.stream().map(m -> (Long) m.get("id")).collect(Collectors.toSet()));

        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("id"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);

        for (Map<String, Object> row : rows) {
            BigDecimal standardPrice = (BigDecimal) row.get("standard_price");
//
//            BigDecimal stockQuantity = materialIdQuantityMap.get((Long) row.get("id"));

            BigDecimal stockQuantity = (BigDecimal)ObjectUtils.defaultIfNull(row.get("stock_quantity"), BigDecimal.ZERO);
            row.put("stock_quantity", stockQuantity);
//
            if (Objects.nonNull(standardPrice)) {
                row.put("stock_quantity_standard_price", stockQuantity.multiply(standardPrice));
            }

            row.put("category_path", categoryService.getPathById((Long) row.get("category_id")));
            row.put("characteristic", characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString((Long) row.get("id"), (Long) row.get("batch_id"))));

            // 可以做一些权限处理，比如将库存金额设置为空； 删选某些数据
        }

        // 添加样式
//        report.getAdditionalInfo().put("css", "#report-list div.card-body-scroll-panel > table > thead > th:nth-child(10) { color: red;}");
//        report.getAdditionalInfo().put("css", "div {color: red;}");
//        report.getAdditionalInfo().put("js", "alert(123)");

        // language=javascript
//        String js = "setTimeout(function () {\n" +
//                "    $('table tr:not(.non-data) td:last-child').each(function () {\n" +
//                "        let id = $(this).parent().data(\"id\")\n" +
//                "     " +
//                "   $(this).append('<a href=\"javascript:;\" onclick=\"showDialog(\\'复制\\', \\''+id+'\\')\">复制</a>')\n" +
//                "    })\n" +
//                "}, 500)";
//
//        report.getAdditionalInfo().put("js", js);
          report.getAdditionalInfo().put("js-operator-column", "{{ openDetailLink('复制', scope.row.id, '复制') }}");
    }
}

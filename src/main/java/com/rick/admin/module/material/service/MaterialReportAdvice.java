package com.rick.admin.module.material.service;

import com.rick.admin.module.core.service.CategoryService;
import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
public class MaterialReportAdvice implements ReportAdvice {

    private final CategoryService categoryService;

    private final StockDAO stockDAO;

    private final MaterialProfileService materialProfileService;

    private final CharacteristicConverter characteristicConverter;

    @Override
    public void beforeQuery(Report report, Map<String, Object> requestMap) {
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
            String specification = characteristicConverter.convert(null, (String) row.get("specification"));
            row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(row.get("characteristic"), ""));

            // 可以做一些权限处理，比如将库存金额设置为空； 删选某些数据
        }

        // 添加样式
//        report.getAdditionalInfo().put("css", "#report-list div.card-body-scroll-panel > table > thead > th:nth-child(10) { color: red;}");
//        report.getAdditionalInfo().put("css", "div {color: red;}");
        report.getAdditionalInfo().put("js", "$('#exportBtn').after('<a class=\"btn btn-secondary mr-2\" href=\"/reports/stock/produce\"><i class=\"fa fa-upload\"></i> 生产库存报表</a>');$('#exportBtn').after('<a class=\"btn btn-secondary mr-2\" href=\"/reports/stock\"><i class=\"fa fa-upload\"></i> 仓库报表</a>'); $('#exportBtn').hide();");

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

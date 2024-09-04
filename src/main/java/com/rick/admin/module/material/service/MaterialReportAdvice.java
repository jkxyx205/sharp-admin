package com.rick.admin.module.material.service;

import com.rick.admin.module.core.service.CategoryService;
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

    private final MaterialProfileService materialProfileService;

    private final CharacteristicConverter characteristicConverter;

    @Override
    public void beforeQuery(Report report, Map<String, Object> requestMap) {
        MaterialKeywordsHelper.keywordsConditionSQLSet(report, requestMap);
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

            if (row.get("batch_id") == null) {
                row.put("batch_id", "");
            }

            // 可以做一些权限处理，比如将库存金额设置为空； 删选某些数据
        }

        // 添加样式
//        report.getAdditionalInfo().put("css", "#report-list div.card-body-scroll-panel > table > thead > th:nth-child(10) { color: red;}");
//        report.getAdditionalInfo().put("css", "div {color: red;}");
//        report.getAdditionalInfo().put("js", "$('#exportBtn').after('<a class=\"btn btn-secondary mr-2\" href=\"/reports/stock/defective\"><i class=\"fa fa-upload\"></i> 次品库存报表</a>');$('#exportBtn').after('<a class=\"btn btn-secondary mr-2\" href=\"/reports/stock/produce\"><i class=\"fa fa-upload\"></i> 线边库存报表</a>');$('#exportBtn').after('<a class=\"btn btn-secondary mr-2\" href=\"/reports/stock\"><i class=\"fa fa-upload\"></i> 材料库存报表</a>'); $('#exportBtn').hide();" +
//                "setTimeout(() => {" +
//                "$('.report-list-table tr').each(function() {\n" +
//                "  let $td = $(this).find('td:eq(8)');\n" +
//                "  let text = $td.text().trim();\n" +
//                "  let batchId = $(this).find('td:eq(11)').text().trim();\n" +
//                "  let materialId = $(this).data('id');" +
//                "  $td.html('<a href=\"javascript:;\" onclick=\"openOnNewTab(\\''+materialId+'\\', \\'reports/699659248728047616?batchId='+batchId+'&material_id='+materialId+'&plantId=719893335619162112&page=1&size=50\\', \\'物料凭证\\')\">'+text+'</a>')\n" +
//                "})}, 1000)");

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
//          report.getAdditionalInfo().put("js-operator-column", "{{ openDetailLink('复制', scope.row.id, '复制') }}");
    }

    /**
     * ajax_list 需要在此处处理js代码， 否则可以在 beforeSetRow 中处理 js代码
     * @param report
     */
    @Override
    public void init(Report report) {
//        report.getAdditionalInfo().put("js", "$('#exportBtn').after('<a class=\"btn btn-secondary mr-2\" href=\"/reports/stock/defective\"><i class=\"fa fa-upload\"></i> 次品库存报表</a>');$('#exportBtn').after('<a class=\"btn btn-secondary mr-2\" href=\"/reports/stock/produce\"><i class=\"fa fa-upload\"></i> 线边库存报表</a>');$('#exportBtn').after('<a class=\"btn btn-secondary mr-2\" href=\"/reports/stock\"><i class=\"fa fa-upload\"></i> 材料库存报表</a>'); $('#exportBtn').hide();" +
//                "function refresh() {$('.report-list-table tr').each(function() {\n" +
//                "  let $td = $(this).find('td[name=stock_quantity]');\n" +
//                "  let text = $td.text().trim();\n" +
//                "  let batchId = $(this).find('input[name=batch_id]').val();\n" +
//                "  let materialId = $(this).data('id');" +
//                "  let materialCode = $(this).find('td[name=code]').text().trim();" +
//                "  let plantId = $(this).find('input[name=plantId]').val();\n" +
//                "  let plantName = $(this).find('td[name=plantName]').text();" +
//                "  if (plantName === '在途') {" +
//                "       $td.html('<a href=\"javascript:;\" onclick=\"openOnNewTab(\\''+materialId+'\\', \\'reports/816284792017412096?batch_id='+batchId+'&material_id='+materialId+'&material_code='+materialCode+'&is_complete=0&page=1&size=50\\', \\'在途库存\\')\">'+text+'</a>')\n" +
//                "   } else {" +
//                "       $td.html('<a href=\"javascript:;\" onclick=\"openOnNewTab(\\''+materialId+'\\', \\'reports/699659248728047616?batchId='+batchId+'&material_id='+materialId+'&plantId='+plantId+'&page=1&size=50\\', \\'物料凭证\\')\">'+text+'</a>')\n" +
//                "   }" +
//                "})}");

        report.getAdditionalInfo().put("js", "$('#exportBtn').after(`<div class=\"dropdown mr-2\">\n" +
                "  <button class=\"btn btn-secondary dropdown-toggle\" type=\"button\" id=\"dropdownMenuButton\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">\n" +
                "    报表导出\n" +
                "  </button>\n" +
                "  <div class=\"dropdown-menu\" aria-labelledby=\"dropdownMenuButton\">\n" +
                "    <a class=\"dropdown-item\" href=\"javascript:;\" onclick=\"$.LoadingOverlay('show');window.download('/reports/stock', function() {$.LoadingOverlay('hide')},function(msg) {toastr.error(msg)})\">材料库</a>\n" +
                "    <a class=\"dropdown-item\" href=\"javascript:;\" onclick=\"$.LoadingOverlay('show');window.download('/reports/stock/produce', function() {$.LoadingOverlay('hide')}, function(msg) {toastr.error(msg)})\">产前库</a>\n" +
//                "    <a class=\"dropdown-item\" href=\"/reports/stock/produce\">产前库</a>\n" +
                "    <a class=\"dropdown-item\" href=\"/reports/stock/machine\">机芯库</a>\n" +
                "    <a class=\"dropdown-item\" href=\"/reports/stock/product\">成品库</a>\n" +
                "    <a class=\"dropdown-item\" href=\"/reports/stock/defective\">次品库</a>\n" +
                "  </div>\n" +
                "</div>`);\n" +
                "$('#exportBtn').hide();\n" +
                "function refresh() {$('.report-list-table tr').each(function() {\n" +
                "  let $td = $(this).find('td[name=stock_quantity]');\n" +
                "  let text = $td.text().trim();\n" +
                "  let batchId = $(this).find('input[name=batch_id]').val();\n" +
                "  let materialId = $(this).data('id');" +
                "  let materialCode = $(this).find('td[name=code]').text().trim();" +
                "  let plantId = $(this).find('input[name=plantId]').val();\n" +
                "  let plantName = $(this).find('td[name=plantName]').text();" +
                "  if (plantName === '在途') {" +
                "       $td.html('<a href=\"javascript:;\" onclick=\"openOnNewTab(\\''+materialId+'\\', \\'reports/816284792017412096?batch_id='+batchId+'&material_id='+materialId+'&material_code='+materialCode+'&is_complete=0&page=1&size=50\\', \\'在途库存\\')\">'+text+'</a>')\n" +
                "   } else {" +
                "       $td.html('<a href=\"javascript:;\" onclick=\"openOnNewTab(\\''+materialId+'\\', \\'reports/699659248728047616?batchId='+batchId+'&material_id='+materialId+'&plantId='+plantId+'&page=1&size=50\\', \\'物料凭证\\')\">'+text+'</a>')\n" +
                "   }" +
                "})}");

        report.getAdditionalInfo().put("js-operator-column", "{{ openDetailLink('复制', scope.row.id, '复制') }}");
    }
}

package com.rick.admin.module.produce.service;

import com.rick.admin.module.material.service.MaterialProfileService;
import com.rick.admin.module.material.service.MaterialProfileSupport;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/7/12 02:55
 */
@Component
@RequiredArgsConstructor
public class ProduceOrderStockReportAdvice implements ReportAdvice {

    private final MaterialProfileService materialProfileService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("id"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);

        for (Map<String, Object> row : rows) {
            row.put("needPurchase",
                    ((BigDecimal)row.get("stock_quantity")).add((BigDecimal)row.get("open_quantity")).subtract((BigDecimal)row.get("quantity")).compareTo(BigDecimal.ZERO) == -1);

            row.put("diffQuantity",
                    ((BigDecimal)row.get("stock_quantity")).subtract((BigDecimal)row.get("quantity")));

            row.put("characteristic", characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString((Long) row.get("id"), (Long) row.get("batch_id"))));
        }

        //report.getAdditionalInfo().put("css", "div {color: red;}");
        // language=JS
        String js = "$('table tr input[name=needPurchase][value=true]').css('color', '#ffffff').parents('tr').css('background', '#f86c6b').css('color', '#ffffff')\n" +
                "$('table tr input[name=needPurchase][value=false]').css('color', '#ffffff').parents('tr').css('background', '#ffc107').css('color', '#ffffff')\n" +
                "$('#batch-cpn-bar .btn-group').html('').append('<button class=\"btn btn-primary\" style=\"padding: .075rem .45rem\" type=\"button\" onclick=\"addPurchase()\" disabled>立即采购...</button>')\n" +
                "function addPurchase() {\n" +
                "    let materialIds = []\n" +
                "    let quantity = []\n" +
                "    $('table > tbody td:nth-child(2) > input[type=checkbox]:checked').each(function () {\n" +
                "        console.log($(this).prop(\"name\"), '...')\n" +
                "        materialIds.push($(this).prop(\"name\") + '@' + $(this).parents('tr').find('[name=batch_id]').val())\n" +
                "        quantity.push(Math.abs($(this).parents('tr').find(\"td\").eq(-1).text().replaceAll(',', '')))\n" +
                "    })\n" +
                "    \n" +
                "    if (!materialIds.length) {\n" +
                "        alert('请选择要采购的物料！')\n" +
                "        return\n" +
                "    }\n" +
                "\n" +
                "    console.log(materialIds)\n" +
                "    openOnNewTab('pur_purchase_order_batch_add', '/produce_orders/purchase_order?materialIds=' + materialIds.join(',') + '&quantity=' + quantity.join(',')" +
                ",  '采购订单');\n" +
                "}";
        report.getAdditionalInfo().put("js", js);
    }
}

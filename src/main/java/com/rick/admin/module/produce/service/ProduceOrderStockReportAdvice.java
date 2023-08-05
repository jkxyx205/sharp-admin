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
        // language=JS
        String js = "$('table tr td:nth-child(11) span:contains(-)').css('color', '#ffffff').parents('tr').css('background', 'rgb(231, 116, 112)').css('color', '#ffffff')\n" +
                "$('#batch-cpn-bar .btn-group').html('').append('<button class=\"btn btn-primary\" style=\"padding: .075rem .45rem\" type=\"button\" onclick=\"addPurchase()\" disabled>立即采购...</button>')\n" +
                "function addPurchase() {\n" +
                "    let materialIds = []\n" +
                "    let quantity = []\n" +
                "    $('table > tbody td:nth-child(2) > input[type=checkbox]:checked').each(function () {\n" +
                "        console.log($(this).prop(\"name\"), '...')\n" +
                "        materialIds.push($(this).prop(\"name\"))\n" +
                "        quantity.push(Math.abs($(this).parents('tr').find(\"td:last-child\").text()))\n" +
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

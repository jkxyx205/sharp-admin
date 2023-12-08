package com.rick.admin.module.produce.service;

import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.module.material.service.CharacteristicConverter;
import com.rick.admin.module.material.service.MaterialProfileService;
import com.rick.admin.module.material.service.MaterialProfileSupport;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/7/12 02:55
 */
@Component
@RequiredArgsConstructor
public class ProduceOrderPurchaseSendAdvice implements ReportAdvice {

    private final MaterialProfileService materialProfileService;

    private final CharacteristicConverter characteristicConverter;

    @Override
    public void beforeQuery(Report report, Map<String, Object> requestMap) {
        boolean purchaseOrderAuthority = UserContextHolder.get().getAuthorityList().contains("pur_purchase_order_add");
        String js = "$('#partner_id').multipleSelect({\n" +
                "    filter: true\n" +
                "});";
        if (purchaseOrderAuthority) {
            // language=JavaScript
            js = js + "$('table tbody tr').each(function() { let $materialTd = $(this).find('td:nth-child(3)'); let materialId = $(this).find('input[name=material_id]').val(); let materialCode = $materialTd.text().trim();$materialTd.html(\"<a href=\\\"javascript:;\\\" name=\\\"material_code\\\" onclick=\\\"showDialog('查看', '\"+materialId+\"')\\\">\"+materialCode+\"</a>\"); let $codeTd = $(this).find('td:nth-child(10)'); let code = $codeTd.text().trim();$codeTd.html(\"<a href=\\\"javascript:;\\\" onclick=\\\"openOnNewTab('\"+code+\"', '/produce_orders/code/\"+code+\"', '\"+code+\"')\\\">\"+code+\"</a>\")});" +
                    "$('#batch-cpn-bar .btn-group').html('').append('<button class=\"btn btn-primary\" style=\"padding: .075rem .45rem\" type=\"button\" onclick=\"addPurchase()\" disabled>立即采购...</button><button class=\"btn btn-primary\" style=\"padding: .075rem .45rem\" type=\"button\" onclick=\"deletePurchase()\" disabled>删除</button>')\n" +
                    "function addPurchase() {\n" +
                    "    let itemIds = getItemIds()\n" +
                    "    if (!itemIds) {\n" +
                    "        return\n" +
                    "    }" +
                    "\n" +
                    "    replaceAndOpenOnNewTab('pur_purchase_order_batch_add_from_purchase_send', '/produce_orders/purchase_order_purchase_send?itemIds=' + itemIds.join(',')" +
                    ",  '采购订单');\n" +
                    "}\n" +
                    "\n" +
                    "function deletePurchase() {\n" +
                    "    let itemIds = getItemIds()\n" +
                    "    if (!itemIds) {\n" +
                    "        return\n" +
                    "    }\n" +
                    "    \n" +
                    "    if (confirm('确定要删除采购申请吗？')) {\n" +
                    "        $.ajax({\n" +
                    "            url: '/purchase_order/requisition?ids=' + itemIds.join(','),\n" +
                    "            type: \"delete\",\n" +
                    "            success: function(){\n" +
                    "                window.location.reload(" +
                    ")\n" +
                    "            }\n" +
                    "        });\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "function getItemIds() {\n" +
                    "    let itemIds = []\n" +
                    "    $('table > tbody tr td:nth-child(2) > input[type=checkbox]:checked').each(function () {\n" +
                    "        console.log($(this).val(), '...')\n" +
                    "        itemIds.push($(this).val())\n" +
                    "    })\n" +
                    "\n" +
                    "    if (!itemIds.length) {\n" +
                    "        alert('请选择要采购的物料！')\n" +
                    "        return\n" +
                    "    }\n" +
                    "\n" +
                    "    console.log(itemIds)\n" +
                    "    return itemIds \n" +
                    "}";
        } else {
            js = "$('table thead tr th:nth-child(2), table tbody tr td:nth-child(2)').hide();$('#batch-cpn-bar .btn-group').hide()";
        }
//        report.setAdditionalInfo(new HashMap<>());
        report.getAdditionalInfo().put("js", js);
        report.getAdditionalInfo().put("css", "td:nth-child(9) > span {max-width: 100px; text-wrap: wrap;}");
    }

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);

        for (Map<String, Object> row : rows) {
            row.put("characteristic", characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id"))));
            String specification = characteristicConverter.convert(null, (String) row.get("specification"));
            row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(row.get("characteristic"), ""));
        }
    }
}

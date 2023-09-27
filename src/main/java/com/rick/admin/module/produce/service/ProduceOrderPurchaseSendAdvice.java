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

import java.util.*;
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
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);

        for (Map<String, Object> row : rows) {
            row.put("characteristic", characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id"))));
            String specification = characteristicConverter.convert(null, (String) row.get("specification"));
            row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(row.get("characteristic"), ""));
        }

        boolean purchaseOrderAuthority = UserContextHolder.get().getAuthorityList().contains("pur_purchase_order_add");
        // language=JS
        String js = "";

        if (purchaseOrderAuthority) {
            js += "$('#batch-cpn-bar .btn-group').html('').append('<button class=\"btn btn-primary\" style=\"padding: .075rem .45rem\" type=\"button\" onclick=\"addPurchase()\" disabled>立即采购...</button>')\n" +
                    "function addPurchase() {\n" +
                    "    let itemIds = []\n" +
                    "    let quantity = []\n" +
                    "    $('table > tbody tr td:nth-child(2) > input[type=checkbox]:checked').each(function () {\n" +
                    "        console.log($(this).val(), '...')\n" +
                    "        itemIds.push($(this).val())\n" +
                    "    })\n" +
                    "    \n" +
                    "    if (!itemIds.length) {\n" +
                    "        alert('请选择要采购的物料！')\n" +
                    "        return\n" +
                    "    }\n" +
                    "\n" +
                    "    console.log(itemIds)\n" +
                    "    replaceAndOpenOnNewTab('pur_purchase_order_batch_add_from_purchase_send', '/produce_orders/purchase_order_purchase_send?itemIds=' + itemIds.join(',')" +
                    ",  '采购订单');\n" +
                    "}";
        } else {
            js += "$('table thead tr th:nth-child(2), table tbody tr td:nth-child(2)').hide();$('#batch-cpn-bar .btn-group').hide()";
        }
        report.setAdditionalInfo(new HashMap<>());
        report.getAdditionalInfo().put("js", js);
    }
}

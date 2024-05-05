package com.rick.admin.module.purchase.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.material.service.CharacteristicConverter;
import com.rick.admin.module.material.service.MaterialProfileService;
import com.rick.admin.module.material.service.MaterialProfileSupport;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
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
 * @date 2024/4/29 10:21
 */
@Component
@RequiredArgsConstructor
public class ListDetailReportAdvice implements ReportAdvice {

    private final MaterialProfileService materialProfileService;

    private final CharacteristicConverter characteristicConverter;

    private final MaterialService materialService;

    private final PurchaseOrderService purchaseOrderService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);
        Set<Long> materialIds = rows.stream().map(row -> (Long) row.get("material_id")).collect(Collectors.toSet());
        Set<Long> itemIds = rows.stream().map(row -> (Long) row.get("id")).collect(Collectors.toSet());

        Map<Long, BigDecimal> historyGoodsReceiptQuantityMap = purchaseOrderService.historyGoodsReceiptQuantity(itemIds);

        materialService.consumeMaterialDescription(materialIds, (idMaterialMap, dictService) -> {
            for (Map<String, Object> row : rows) {
                row.put("materialName", idMaterialMap.get(row.get("material_id")).getName());
                String specification = idMaterialMap.get(row.get("material_id")).getSpecificationText();
                String characteristic = characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id")));
//                String specification = characteristicConverter.convert(null, (String) row.get("specification"));
                row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(characteristic, ""));
                row.put("unit_price", ((BigDecimal)row.get("unit_price")).stripTrailingZeros().toPlainString());
                row.put("total_price", ((BigDecimal)row.get("total_price")).stripTrailingZeros().toPlainString());

                row.put("goodsReceiptQuantity", historyGoodsReceiptQuantityMap.get(row.get("id")).stripTrailingZeros().toPlainString());
                if (1L == (Long) row.get("is_complete")) {
                    row.put("openQuantity", "0");

                    if ("0".equals(row.get("goodsReceiptQuantity"))) {
                        row.put("goodsReceiptQuantity", row.get("quantity"));
                    }
                } else {
                    BigDecimal openQuantity = ((BigDecimal) row.get("quantity")).subtract(historyGoodsReceiptQuantityMap.get(row.get("id")));
                    row.put("openQuantity", (openQuantity.equals(BigDecimal.ZERO) ? "": "-") + (BigDecimalUtils.lt(openQuantity, BigDecimal.ZERO) ? BigDecimal.ZERO : openQuantity.stripTrailingZeros().toPlainString()));
                }
            }
        });
    }

    @Override
    public void init(Report report) {
        report.getAdditionalInfo().put("js", "$('table tr span[name=is_complete]').each(function() {\n" +
                "    if ($(this).text() === '是') {\n" +
                "        $(this).css('color', 'rgb(77, 189, 116)')\n" +
                "    } else {\n" +
                "        $(this).css('color', 'rgb(248, 108, 107)')\n" +
                "    }\n" +
                "});" +
                "$('table tr span[name=goodsReceiptQuantity]').parent().css('background-color', 'rgb(77, 189, 116)');\n" +
                "$('table tr span[name=openQuantity]').parent().css('background-color', 'rgb(248, 108, 107)');");
    }
}

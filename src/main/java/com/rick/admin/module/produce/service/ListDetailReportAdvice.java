package com.rick.admin.module.produce.service;

import com.rick.admin.common.BigDecimalUtils;
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
@Component("produceListDetailReportAdvice")
@RequiredArgsConstructor
public class ListDetailReportAdvice implements ReportAdvice {

    private final MaterialProfileService materialProfileService;

    private final MaterialService materialService;

    private final ProduceOrderService produceOrderService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);
        Set<Long> materialIds = rows.stream().map(row -> (Long) row.get("material_id")).collect(Collectors.toSet());
        Set<Long> itemIds = rows.stream().map(row -> (Long) row.get("id")).collect(Collectors.toSet());

        Map<Long, BigDecimal> historyGoodsIssueQuantityMap =  produceOrderService.salesHistoryGoodsIssueQuantity(itemIds);

        materialService.consumeMaterialDescription(materialIds, (idMaterialMap, dictService) -> {
            for (Map<String, Object> row : rows) {
                row.put("materialName", idMaterialMap.get(row.get("material_id")).getName());
                String specification = idMaterialMap.get(row.get("material_id")).getSpecificationText();
                String characteristic = characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id")));
                row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(characteristic, ""));
                row.put("unit_price", ((BigDecimal)row.get("unit_price")).stripTrailingZeros().toPlainString());
                row.put("total_price", ((BigDecimal)row.get("total_price")).stripTrailingZeros().toPlainString());

                row.put("goodsIssueQuantity", historyGoodsIssueQuantityMap.get(row.get("id")).stripTrailingZeros().toPlainString());
                if (1L == ((Number) row.get("is_complete")).intValue()) {
                    row.put("openQuantity", "0");

                    if ("0".equals(row.get("GoodsIssueQuantity"))) {
                        row.put("goodsIssueQuantity", row.get("quantity"));
                    }
                } else {
                    BigDecimal openQuantity = ((BigDecimal) row.get("quantity")).subtract(historyGoodsIssueQuantityMap.get(row.get("id")));
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
                "$('table tr span[name=goodsIssueQuantity]').parent().css('background-color', 'rgb(77, 189, 116)');\n" +
                "$('table tr span[name=openQuantity]').parent().css('background-color', 'rgb(248, 108, 107)');");
    }

    public static void main(String[] args) {
        String[] selectS = StringUtils.substringBetween("SELECT produce_order_item.id, produce_order.partner_id, produce_order.id order_id, produce_order.code, produce_order_item.material_id, produce_order_item.material_code, produce_order_item.batch_id, produce_order_item.batch_code, produce_order_item.quantity, produce_order_item.unit, ifnull(produce_order_item.unit_price, 0) unit_price, (produce_order_item.quantity * ifnull(produce_order_item.unit_price, 0)) total_price, produce_order.create_time, produce_order_item.delivery_date, (produce_order_item.is_complete + 0) is_complete FROM produce_order_item, produce_order where produce_order_item.produce_order_code = produce_order.code", "SELECT ", " FROM").split("\\s*,\\s*");

        System.out.println(selectS);

    }
}

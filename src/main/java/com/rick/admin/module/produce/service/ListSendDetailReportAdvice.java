package com.rick.admin.module.produce.service;

import com.rick.admin.module.material.service.MaterialProfileService;
import com.rick.admin.module.material.service.MaterialProfileSupport;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.common.util.BigDecimalUtils;
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
@Component("listSendDetailReportAdvice")
@RequiredArgsConstructor
public class ListSendDetailReportAdvice implements ReportAdvice {

    private final MaterialProfileService materialProfileService;

    private final MaterialService materialService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);
        Set<Long> materialIds = rows.stream().map(row -> (Long) row.get("material_id")).collect(Collectors.toSet());

        materialService.consumeMaterialDescription(materialIds, (idMaterialMap, dictService) -> {
            for (Map<String, Object> row : rows) {
                row.put("materialName", idMaterialMap.get(row.get("material_id")).getName());
                String specification = idMaterialMap.get(row.get("material_id")).getSpecificationText();
                String characteristic = characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id")));
                row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(characteristic, ""));
                row.put("unit_price", BigDecimalUtils.formatBigDecimalValue(((BigDecimal)row.get("unit_price"))));
                row.put("total_price", BigDecimalUtils.formatBigDecimalValue(((BigDecimal)row.get("total_price"))));
            }
        });
    }

}

package com.rick.admin.module.produce.service;

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
 * @date 2023/9/20 23:55
 */
@Component
@RequiredArgsConstructor
public class ProduceScheduleReportAdvice implements ReportAdvice {

    private final MaterialProfileService materialProfileService;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("materialId"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);

        for (Map<String, Object> row : rows) {
            row.put("characteristic", characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString((Long) row.get("materialId"), (Long) row.get("batch_id"))));

            String specification = (String) row.get("specification");
            row.put("specification", specification);
            row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(row.get("characteristic"), ""));
        }
    }
}

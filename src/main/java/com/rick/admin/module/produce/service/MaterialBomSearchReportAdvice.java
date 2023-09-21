package com.rick.admin.module.produce.service;

import com.rick.admin.module.material.service.CharacteristicConverter;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/9/20 23:05
 */
@Component
@RequiredArgsConstructor
public class MaterialBomSearchReportAdvice implements ReportAdvice {

    private final CharacteristicConverter characteristicConverter;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            String specification = characteristicConverter.convert(null, (String) row.get("specification"));
            row.put("specification", specification);
            row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(row.get("characteristic"), ""));
        }
    }
}

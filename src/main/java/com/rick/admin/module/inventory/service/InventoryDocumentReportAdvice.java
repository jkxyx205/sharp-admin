package com.rick.admin.module.inventory.service;

import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/12 14:42
 */
@Component
@AllArgsConstructor
public class InventoryDocumentReportAdvice implements ReportAdvice {

    final MaterialDAO materialDAO;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
        Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(rows.stream().map(row -> (Long) row.get("material_id")).collect(Collectors.toSet()));
        for (Map<String, Object> row : rows) {
            Material material = idMaterialMap.get((Long) row.get("material_id"));
            row.put("material_code", material.getCode());
            row.put("material_text", material.getName() + " " + material.getCharacteristicText());
            row.put("quantity", (Objects.equals(row.get("movement_type"), "INBOUND") ? "+" : "-") + row.get("quantity"));
        }
    }
}

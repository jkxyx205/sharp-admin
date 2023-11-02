package com.rick.admin.module.inventory.service;

import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.service.CharacteristicConverter;
import com.rick.admin.module.material.service.MaterialProfileService;
import com.rick.admin.module.material.service.MaterialProfileSupport;
import com.rick.db.service.support.Params;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/12 14:42
 */
@Component
@AllArgsConstructor
public class InventoryDocumentReportAdvice implements ReportAdvice {

    final MaterialDAO materialDAO;

    final MaterialProfileService materialProfileService;

    private final CharacteristicConverter characteristicConverter;

    @Override
    public void beforeSetRow(Report report, List<Map<String, Object>> rows) {
//        Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(rows.stream().map(row -> (Long) row.get("material_id")).collect(Collectors.toSet()));

        // is_deleted = :tmp 给条件但是没有赋值作用是查询不需要带 is_deleted = 0， 而是查询所有的记录
        Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(Params.builder(1).pv("ids", rows.stream().map(row -> (Long) row.get("material_id")).collect(Collectors.toSet())).build(),
                "id IN (:ids) AND is_deleted = :tmp");
        Set<String> materialIdBatchIdStringCollection = rows.stream().map(row -> MaterialProfileSupport.materialIdBatchIdString((Long) row.get("material_id"), (Long) row.get("batch_id"))).collect(Collectors.toSet());
        Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);

        for (Map<String, Object> row : rows) {
            Material material = idMaterialMap.get((Long) row.get("material_id"));
            if (Objects.nonNull(material)) {
                row.put("material_code", material.getCode());
                row.put("materialName", material.getName());
                row.put("materialSpecification", material.getSpecificationText());
                row.put("quantity", (Objects.equals(row.get("movement_type"), "INBOUND") ? "+" : "-") + row.get("quantity"));
                row.put("characteristic", characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString(material.getId(), (Long) row.get("batch_id"))));
            }

            String specification = (String) row.get("materialSpecification");
            row.put("specificationAndCharacteristic", (StringUtils.isBlank(specification) ? "" : specification + " ") + Objects.toString(row.get("characteristic"), ""));

        }
    }
}

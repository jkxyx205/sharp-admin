package com.rick.admin.module.produce.service;

import com.rick.admin.module.material.service.MaterialProfileService;
import com.rick.admin.module.material.service.MaterialProfileSupport;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.report.core.entity.Report;
import com.rick.report.core.service.ReportAdvice;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/9/20 23:55
 */
@Component
@RequiredArgsConstructor
public class ProduceScheduleReportAdvice implements ReportAdvice {

    private final MaterialProfileService materialProfileService;

    private final SharpService sharpService;

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

        // 获取已经领过料的生产单号
        Set<String> scheduleCodes = rows.stream().map(row -> row.get("code").toString()).collect(Collectors.toSet());
        List<String> haveMaterialScheduleCodes = sharpService.query("select distinct reference_code from inv_document where type = 'MOVING_TO_PRODUCE' AND reference_code in (:scheduleCodes)",
                Params.builder(1).pv("scheduleCodes", scheduleCodes).build(),
                String.class);

        if (CollectionUtils.isNotEmpty(haveMaterialScheduleCodes) && report.getAdditionalInfo() != null) {
            report.getAdditionalInfo().put("js", "const haveMaterialScheduleCodes = '"+String.join(",", haveMaterialScheduleCodes)+"';" +
                    "$('table td span[name=code]').each(function() {\n" +
                    "\tif (haveMaterialScheduleCodes.indexOf($(this).text()) > -1) {\n" +
                    "\t\t$(this).parents('tr').find('.have-material').css('color', 'rgb(248, 108, 107)').css('font-weight', 'bold')\n" +
                    "\t}\t\n" +
                    "})");
        }

    }

    @Override
    public void init(Report report) {
        if (report.getAdditionalInfo() == null) {
            report.setAdditionalInfo(new HashMap<>());
        }
    }
}

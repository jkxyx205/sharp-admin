package com.rick.admin.module.material.service;

import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.entity.Material;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.expression.Numbers;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Rick.Xu
 * @date 2023/6/2 11:40
 */
@Component("materialFormAdvice")
@RequiredArgsConstructor
public class MaterialFormAdvice implements FormAdvice {

    private final DictService dictService;

    private final StockDAO stockDAO;

    private final MaterialService materialService;

    private final MaterialDAO materialDAO;

    private final SharpService sharpService;


    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        // code处理
        String code = (String) values.get("code");
        String materialType = (String) values.get("materialType");
        if (StringUtils.isBlank(code)) {
            Optional<String> remarkOptional = sharpService.queryForObject("select remark from sys_dict where type = :type AND name = :name",
                    Params.builder(2).pv("type", "MATERIAL_TYPE").pv("name", materialType)
                            .build(),
                    String.class);

            Long currentNumber = Long.parseLong(remarkOptional.get());
            Long newNumber = ++currentNumber;
            values.put("code", materialType.charAt(0) + String.format("%05d", newNumber));

            sharpService.getNamedJdbcTemplate().update("update sys_dict set remark = :newNumber where type = :type AND name = :name",
                    Params.builder(2).pv("type", "MATERIAL_TYPE").pv("name", materialType).pv("newNumber", newNumber)
                            .build());
        }

        // checkbox List => boolean
        if (CollectionUtils.isEmpty((Collection<?>) values.get("batchManagement"))) {
            values.put("batchManagement", false);
        } else {
            values.put("batchManagement", true);
        }

        if (CollectionUtils.isEmpty((Collection<?>) values.get("serialManagement"))) {
            values.put("serialManagement", false);
        } else {
            values.put("serialManagement", true);
        }

        HashMap<String, String> mrpMap = (HashMap<String, String>) values.get("mrp");
        String safetyStockQuantity = mrpMap.get("safetyStockQuantity");
        String maximumStockQuantity = mrpMap.get("maximumStockQuantity");
        values.put("mrp", new Material.Mrp(StringUtils.isNotEmpty(safetyStockQuantity) ? new BigDecimal(safetyStockQuantity) : null,
                StringUtils.isNotEmpty(maximumStockQuantity) ? new BigDecimal(maximumStockQuantity) : null));

    }

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

    }

    @Override
    public void beforeGetInstance(Long instanceId, Map<String, Object> valueMap) {
        Numbers numbers = new Numbers(Locale.CHINA);

        BigDecimal stockQuantity = stockDAO.getStockQuantityByMaterialId((Long)valueMap.get("id"));
        valueMap.put("stockQuantity", stockQuantity + " " + dictService.getDictByTypeAndName("unit", valueMap.get("base_unit").toString()).get().getLabel());
        BigDecimal standardPrice = (BigDecimal) valueMap.get("standardPrice");

        if (Objects.nonNull(standardPrice)) {
            valueMap.put("stockQuantityPrice", numbers.formatDecimal(stockQuantity.multiply(standardPrice), 1, "COMMA", 2, "POINT") + " 元");
        }
    }

    @Override
    public boolean insertOrUpdate(Map<String, Object> values) {
        Material material = materialDAO.mapToEntity(values);
        if (Objects.equals(material.getBatchManagement(), Boolean.TRUE)) {
            // 批次物料加上颜色特征
            material.setClassificationList(Arrays.asList(
                    Classification.builder()
                            .classificationCode("COLOR")
                            .characteristicValueList(Arrays.asList(
                                    CharacteristicValue.builder()
                                            .characteristicCode("COLOR")
                                            .build()
                            ))
                            .build()));
        }


        materialService.saveOrUpdate(material);
        return true;
    }
}

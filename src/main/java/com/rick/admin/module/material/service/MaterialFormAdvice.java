package com.rick.admin.module.material.service;

import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.admin.module.material.entity.Material;
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

    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
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
}

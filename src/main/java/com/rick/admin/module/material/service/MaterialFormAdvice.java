package com.rick.admin.module.material.service;

import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.expression.Numbers;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/6/2 11:40
 */
@Component("materialFormAdvice")
@RequiredArgsConstructor
public class MaterialFormAdvice implements FormAdvice {

    private final DictService dictService;

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
    }

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

    }

    @Override
    public void beforeGetInstance(Long instanceId, Map<String, Object> valueMap) {
        Numbers numbers = new Numbers(Locale.CHINA);
        valueMap.put("stockQuantity", "1299.99 " + dictService.getDictByTypeAndName("unit", valueMap.get("base_unit").toString()).get().getLabel());

        BigDecimal standardPrice = (BigDecimal) valueMap.get("standardPrice");

        if (Objects.nonNull(standardPrice)) {
            valueMap.put("stockQuantityPrice", numbers.formatDecimal(new BigDecimal("1299.99").multiply(BigDecimal.valueOf(2)), 1, "COMMA", 2, "POINT") + " 元");
        }
    }
}

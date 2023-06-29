package com.rick.admin.module.core.service;

import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/1 14:15
 */
@Component("partnerFormService")
@RequiredArgsConstructor
public class PartnerFormService implements FormAdvice {

    private final DictService dictService;

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        dictService.rebuild("core_partner");
        dictService.rebuild("core_partner_vendor");
    }
}

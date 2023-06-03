package com.rick.admin.sys.user.service;

import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/3 08:50
 */
@Component
@RequiredArgsConstructor
public class UserFormAdvice implements FormAdvice {

    private final DictService dictService;

    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

    }

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        dictService.rebuild("sys_user");
    }
}

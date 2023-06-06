package com.rick.admin.module.core.service;

import com.rick.admin.common.DictDOSupplierImpl;
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
@Component("categoryFormService")
@RequiredArgsConstructor
public class CategoryFormService implements FormAdvice {

    private final DictService dictService;

    private final CategoryService categoryService;

    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

    }

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        categoryService.rebuild();
        dictService.rebuild("core_material_category");
        dictService.rebuild(DictDOSupplierImpl.CATEGORY_PATH);
    }
}

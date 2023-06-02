package com.rick.admin.module.core.service;

import com.rick.meta.dict.convert.ValueConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Rick.Xu
 * @date 2023/6/1 15:49
 */
@Component("categoryValueConverter")
@RequiredArgsConstructor
public class CategoryValueConverter implements ValueConverter<Long> {

    private final CategoryService categoryService;

    @Override
    public String convert(Object context, Long value) {
        return categoryService.getPathById(value);
    }
}

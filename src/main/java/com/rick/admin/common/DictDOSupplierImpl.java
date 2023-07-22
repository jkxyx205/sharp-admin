package com.rick.admin.common;

import com.google.common.collect.Lists;
import com.rick.admin.module.core.service.CategoryService;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictDOSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/7 00:23
 */
@Component
@RequiredArgsConstructor
public class DictDOSupplierImpl implements DictDOSupplier {

    private final CategoryService categoryService;

    public final static String CATEGORY_PATH = "category_path";

    @Override
    public List<Dict> get() {
        // category
        List<Dict> dictList = Lists.newArrayListWithExpectedSize(categoryService.getIdCategoryPathMap().size());

        for (Map.Entry<Long, String> entry : categoryService.getIdCategoryPathMap().entrySet()) {
            dictList.add(new Dict(CATEGORY_PATH, String.valueOf(entry.getKey()), entry.getValue(), 1));
        }

        return dictList;
    }
}

package com.rick.admin.module.core.service;

import com.rick.admin.module.core.dao.CategoryDAO;
import com.rick.admin.module.core.entity.Category;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/1 15:57
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class CategoryService implements InitializingBean {

    static final String CATEGORY_SEPARATOR = "»";

    CategoryDAO categoryDAO;

    Map<Long, String> idCategoryPathMap = new HashMap<>();

    public String getPathById(Long id) {
        if (id == 0) {
            return null;
        }

        return idCategoryPathMap.get(id);
    }

    public void rebuild() {
        idCategoryPathMap.clear();
        List<Category> categoryList = categoryDAO.selectAll();

        Map<Long, Category> idNameMap = categoryList.stream().collect(Collectors.toMap(Category::getId, c -> c));

        for (Category category : categoryList) {
            String name = recursionName(category, idNameMap);

            String[] path = name.split(CATEGORY_SEPARATOR);
            ArrayUtils.reverse(path);
            idCategoryPathMap.put(category.getId(), StringUtils.join(path, CATEGORY_SEPARATOR));
        }
    }

    @Override
    public void afterPropertiesSet() {
        this.rebuild();
    }

    private String recursionName(Category category, Map<Long, Category> idNameMap) {
        String name = category.getName();
        Long parentId = category.getParentId();
        if (parentId != 0) {
            name += CATEGORY_SEPARATOR + recursionName(idNameMap.get(parentId), idNameMap);
        }

       return name;
    }
}
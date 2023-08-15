package com.rick.admin.init;

import com.rick.admin.module.core.dao.CategoryDAO;
import com.rick.admin.module.core.entity.Category;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.service.MaterialFormAdvice;
import com.rick.excel.core.ExcelReader;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/15 10:09
 */
@SpringBootTest
public class MaterialDataHandler {

    @Resource
    private MaterialFormAdvice materialFormAdvice;

    @Resource
    private MaterialDAO materialDAO;

    @Resource
    private CategoryDAO categoryDAO;

    @Resource
    private DictService dictService;

    @Test
    public void importData() throws Exception {
        Map<String, Long> categoryNameIdMap = categoryDAO.selectAll().stream().collect(Collectors.toMap(Category::getName, Category::getId));
        Map<String, String> unitLabelNameMap = dictService.getDictByType("unit").stream().collect(Collectors.toMap(Dict::getLabel, Dict::getName));

        String path = "/Users/rick/Space/Yodean/苏州普源电机/data/Material.xlsx";
        ExcelReader.readExcelContent(new FileInputStream(path), (index, data, sheetIndex, sheetName) -> {
            if (data.length == 0) {
                return false;
            }

            if (index > 0) {
                String specification = Objects.toString(data[2], "");
                List<List<String>> specificationList = new ArrayList<>();
                if (StringUtils.isNotBlank(specification)) {
                    specificationList.add(Arrays.asList("规格", specification));
                }

                Boolean batchManagement = (data.length == 5) ? (Objects.equals(data[4], "1") || Objects.equals(String.valueOf(data[4]), "1.0")) : false;

                Map<String, Object> values = new HashMap<>();
                values.put("code", data[0]);
                values.put("name", data[1]);
                values.put("specificationList", specificationList);
                values.put("baseUnit", unitLabelNameMap.get((String) data[3]));
                values.put("batchManagement", batchManagement ? Lists.newArrayList(true) : null);

                values.put("materialType", "ROH");
                values.put("categoryId", categoryNameIdMap.get(sheetName));

                materialFormAdvice.beforeInstanceHandle(null, null, values);
                materialDAO.insertOrUpdate(values);
            }

            return true;
        });

    }
}

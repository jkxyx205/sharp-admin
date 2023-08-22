package com.rick.admin.init;

import com.rick.admin.module.core.dao.CategoryDAO;
import com.rick.admin.module.core.entity.Category;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.service.ClassificationService;
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
import java.math.BigDecimal;
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

    @Resource(name = "materialClassificationService")
    private ClassificationService classificationService;

    @Test
    public void importData() throws Exception {
        materialDAO.delete(Collections.emptyMap(), "material_type = 'ROH'");
        Map<String, Long> categoryNameIdMap = categoryDAO.selectAll().stream().collect(Collectors.toMap(Category::getName, Category::getId));
        Map<String, String> unitLabelNameMap = dictService.getDictByType("unit").stream().collect(Collectors.toMap(Dict::getLabel, Dict::getName));

        String path = "/Users/rick/Space/tmp/py/data/material.xlsx";
        ExcelReader.readExcelContent(new FileInputStream(path), (index, data, sheetIndex, sheetName) -> {
            if (data.length < 4 || StringUtils.isBlank((CharSequence) data[1]) || StringUtils.isBlank((CharSequence) data[3])) {
                return false;
            }

            if (index > 0) {
                String specification = dataToString(data[2]);
                List<List<String>> specificationList = new ArrayList<>();
                if (StringUtils.isNotBlank(specification)) {
                    specificationList.add(Arrays.asList("规格", specification));
                }

                Boolean batchManagement = (data.length == 5) ? (Objects.equals(data[4], "1") || Objects.equals(String.valueOf(data[4]), "1.0")) : false;

                Map<String, Object> values = new HashMap<>();
                values.put("code", data[0]);
                values.put("name", data[1]);
                values.put("specificationList", specificationList);

                String unitLabel = (String) data[3];
                if (unitLabel.matches("[a-zA-z]+")) {
                    unitLabel = unitLabel.toUpperCase();
                }

                values.put("baseUnit", unitLabelNameMap.get(unitLabel));

                values.put("batchManagement", batchManagement ? Lists.newArrayList(true) : null);

                values.put("materialType", "ROH");
                values.put("categoryId", categoryNameIdMap.get(sheetName));

                materialFormAdvice.beforeInstanceHandle(null, null, values);
                materialDAO.insertOrUpdate(values);
            }

            return true;
        });

    }

//    @Test
    public void batchAssignClassification() {
        classificationService.batchAssignClassification("COLOR", Arrays.asList(725426617400188928L));
    }

    private String dataToString(Object data) {
        if (Objects.isNull(data)) {
            return null;
        }

        if (data instanceof Number) {
           Number number = (Number) data;
           return new BigDecimal(number.toString()).stripTrailingZeros().toPlainString();
        }

        return String.valueOf(data);
    }

}

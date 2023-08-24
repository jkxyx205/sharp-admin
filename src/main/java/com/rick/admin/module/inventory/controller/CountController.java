package com.rick.admin.module.inventory.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.admin.module.core.entity.Classification;
import com.rick.admin.module.material.dao.ClassificationDAO;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.util.JsonUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.service.SharpService;
import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelCell;
import com.rick.excel.core.model.ExcelRow;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/16 09:14
 */
@RestController
@RequestMapping("inventory/count")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CountController {

    SharpService sharpService;

    DictService dictService;

    ClassificationDAO materialClassificationDAO;

    @GetMapping("template")
    public void downloadCountTemplate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = "盘点单" + Time2StringUtils.format(new Date());
        ExcelWriter excelWriter = new ExcelWriter(fileName);
        float heightInPoints = 30F;

        Map<Long, List<Classification>> materialIdClassificationMap = materialClassificationDAO.findClassificationByMaterialIds(null);

        ExcelCell title = new ExcelCell(1, 1, "盘点单", 1, 7);
        title.setHeightInPoints(heightInPoints);
        title.setStyle(createTitleStyle(excelWriter.getBook()));
        excelWriter.writeCell(title);

        ExcelRow label = new ExcelRow(1, 2, new Object[]{"分类", "物料编号", "名称", "规格", "特征值", "单位", "数量"});
        label.setHeightInPoints(heightInPoints);
        label.setStyle(createLabelStyle(excelWriter.getBook()));
        excelWriter.writeRow(label);

        excelWriter.getActiveSheet().setColumnWidth(0, 4000);
        excelWriter.getActiveSheet().setColumnWidth(1, 3000);
        excelWriter.getActiveSheet().setColumnWidth(2, 6000);
        excelWriter.getActiveSheet().setColumnWidth(3, 6000);
        excelWriter.getActiveSheet().setColumnWidth(4, 6000);
        excelWriter.getActiveSheet().setColumnWidth(5, 3000);
        excelWriter.getActiveSheet().setColumnWidth(6, 3000);

        String sql = "select core_material_category.order_index, core_material_category.name categoryName, mm_material.id, mm_material.code, mm_material.name, mm_material.`specification`, mm_material.base_unit  from core_material_category left join mm_material on core_material_category.id = mm_material.category_id where mm_material.code is not null order by order_index asc";
        List<Map<String, Object>> list = sharpService.query(sql, null);
        Map<String, List<Map<String, Object>>> categroyNameMaterialMap = list.stream().collect(Collectors.groupingBy(r -> r.get("order_index") + "-" + r.get("categoryName")));
        Set<String> treeSet = new TreeSet<>(categroyNameMaterialMap.keySet());

        int y = 3;
        for (String key : treeSet) {
            List<Map<String, Object>> valueList = categroyNameMaterialMap.get(key);
            int valueSize = valueList.size();

            Map<Long, List<String>> characteristicValueListMap = Maps.newHashMap();

            // 获取特征值
            int characteristicValueListSize = 0;
            int classificationListNumber = 0;
            for (int i = 0; i < valueSize; i++) {
                Map<String, Object> value = valueList.get(i);
                Long materialId  = (Long) value.get("id");
                List<Classification> classificationList = materialIdClassificationMap.get(materialId);
                List<String> characteristicValueList;
                if (CollectionUtils.isNotEmpty(classificationList)) {
                    List<List<String>> optionList = classificationList.stream().flatMap(classification -> classification.getCharacteristicList().stream())
                            .map(characteristic -> characteristic.getCpnConfigurer().getOptions().stream().map(CpnConfigurer.CpnOption::getLabel).collect(Collectors.toList())).collect(Collectors.toList());
                    characteristicValueList = Lists.newArrayList();
                    getCharacteristicValueList(characteristicValueList, optionList, 0, "");
                    characteristicValueListMap.put(materialId, characteristicValueList);
                    characteristicValueListSize += characteristicValueList.size();
                    classificationListNumber++;
                }
            }

            ExcelCell categoryCell = new ExcelCell(1, y, key.substring(key.indexOf("-") + 1), valueSize + characteristicValueListSize - classificationListNumber, 1);
            categoryCell.setStyle(createCellStyle(excelWriter.getBook()));
            excelWriter.writeCell(categoryCell);

            for (int i = 0; i < valueSize; i++) {
                Map<String, Object> value = valueList.get(i);

                List<String> characteristicValueList = characteristicValueListMap.get(value.get("id"));
                if (CollectionUtils.isNotEmpty(characteristicValueList)) {
                    for (String characteristicValue : characteristicValueList) {
                        ExcelRow dataRow = new ExcelRow(2, y, new Object[]{
                                value.get("code"), value.get("name"), getSpecificationText((String) value.get("specification")),
                                characteristicValue,
                                dictService.getDictByTypeAndName("unit", (String) value.get("base_unit")).get().getLabel(), ""
                        });
                        dataRow.setHeightInPoints(heightInPoints);
                        dataRow.setStyle(createCellStyle(excelWriter.getBook()));
                        excelWriter.writeRow(dataRow);

                        y++;
                    }

                } else {
                    ExcelRow dataRow = new ExcelRow(2, y, new Object[]{
                            value.get("code"), value.get("name"), getSpecificationText((String) value.get("specification")),
                            "",
                            dictService.getDictByTypeAndName("unit", (String) value.get("base_unit")).get().getLabel(), ""
                    });
                    dataRow.setHeightInPoints(heightInPoints);
                    dataRow.setStyle(createCellStyle(excelWriter.getBook()));
                    excelWriter.writeRow(dataRow);

                    y++;
                }


            }
        }

        excelWriter.toFile(HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, fileName + ".xlsx"));
    }

    private String getSpecificationText(String specification) {
        List<List> specificationList = JsonUtils.toList(specification, List.class);
        return CollectionUtils.isNotEmpty(specificationList) ? specificationList.stream().map(list -> list.get(1).toString()).collect(Collectors.joining("/")) : "";
    }

    private XSSFCellStyle createCellStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();

        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }

    private XSSFCellStyle createLabelStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();
        XSSFColor color = new XSSFColor(Color.lightGray, new DefaultIndexedColorMap());
        // 填充色
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 文字
        XSSFFont font = book.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private XSSFCellStyle createTitleStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();
        XSSFColor color = new XSSFColor(Color.lightGray, new DefaultIndexedColorMap());
        // 填充色
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 文字
        XSSFFont font = book.createFont();
        font.setBold(true);
        font.setFontHeight(23);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private XSSFCellStyle createStyle(XSSFWorkbook book) {
        XSSFCellStyle cellStyle = book.createCellStyle();

        // 定义颜色
        XSSFColor color = new XSSFColor(java.awt.Color.BLUE, new DefaultIndexedColorMap());
        XSSFColor color2 = new XSSFColor(Color.RED, new DefaultIndexedColorMap());
        XSSFColor color3 = new XSSFColor(Color.GREEN, new DefaultIndexedColorMap());

        // 填充色
        cellStyle.setFillForegroundColor(color2);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 文字色
        XSSFFont font = book.createFont();
        font.setColor(color);
        font.setFontHeight(23);
        cellStyle.setFont(font);

        // 设置边框(合并这个不生效) 需要单独在CellRangeAddress设置
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, color3);

        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return cellStyle;
    }

    private void getCharacteristicValueList(List<String> characteristicValueList, List<List<String>> data, int index, String value) {
        if (index >= data.size()) {
            return;
        }

        for (String s : data.get(index)) {
            if (index == data.size() - 1) {
                characteristicValueList.add((value + " ") + s);
            }
            getCharacteristicValueList(characteristicValueList, data, index + 1, (StringUtils.isBlank(value) ? "" : value + " ") + s);
        }
    }
}

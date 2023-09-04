package com.rick.admin.module.report.controller;

import com.rick.admin.common.api.ExcelCellStyleHelper;
import com.rick.admin.module.core.dao.CategoryDAO;
import com.rick.admin.module.core.entity.Category;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.report.model.StockItem;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelCell;
import com.rick.excel.core.model.ExcelRow;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存报表
 * @author Rick.Xu
 * @date 2023/9/4 10:09
 */
@RestController
@RequestMapping("reports/stock")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StockController {

    SharpService sharpService;

    MaterialService materialService;

    BatchService batchService;

    CategoryDAO categoryDAO;

    @GetMapping
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = "材料仓库库存表 " + Time2StringUtils.format(new Date());
        download(request, response, Arrays.asList(719893335619162112L), fileName);
    }

    @GetMapping("produce")
    public void downloadProduceStock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = "生产库存表 " + Time2StringUtils.format(new Date());
        download(request, response, Arrays.asList(726158903766683648L, 726158996003622912L, 726159086739001344L), fileName);
    }

    private void download(HttpServletRequest request, HttpServletResponse response, Collection<Long> plantId, String fileName) throws IOException {
        Map<String, Map<String, List<StockItem>>> data = data(plantId);


        ExcelWriter excelWriter = new ExcelWriter(fileName);
        float heightInPoints = 30F;

        ExcelCell title = new ExcelCell(1, 1, fileName, 1, 7);
        title.setHeightInPoints(heightInPoints);
        title.setStyle(ExcelCellStyleHelper.createTitleStyle(excelWriter.getBook()));
        excelWriter.writeCell(title);

        ExcelRow label = new ExcelRow(1, 2, new Object[]{"分类", "名称", "物料编号", "规格", "特征值", "数量", "单位"});
        label.setHeightInPoints(heightInPoints);
        label.setStyle(ExcelCellStyleHelper.createLabelStyle(excelWriter.getBook()));
        excelWriter.writeRow(label);

        excelWriter.getActiveSheet().setColumnWidth(0, 4000);
        excelWriter.getActiveSheet().setColumnWidth(1, 10000);
        excelWriter.getActiveSheet().setColumnWidth(2, 3000);
        excelWriter.getActiveSheet().setColumnWidth(3, 8000);
        excelWriter.getActiveSheet().setColumnWidth(4, 4000);
        excelWriter.getActiveSheet().setColumnWidth(5, 3000);
        excelWriter.getActiveSheet().setColumnWidth(6, 3000);

        int col1Index = 3;
        for (Map.Entry<String, Map<String, List<StockItem>>> entry : data.entrySet()) {
            Map<String, List<StockItem>> stockItemListMap = entry.getValue();

            int col1RowSpan = stockItemListMap.values().stream().flatMap(stockItems -> stockItems.stream()).collect(Collectors.toList()).size();

            ExcelCell categoryCell = new ExcelCell(1, col1Index, heightInPoints, entry.getKey().substring(entry.getKey().indexOf("-") + 1), col1RowSpan, 1);
            categoryCell.setStyle(ExcelCellStyleHelper.createRowMergeCellStyle(excelWriter.getBook()));
            excelWriter.writeCell(categoryCell);

            int col2Index = col1Index;
            int i = 0;
            for (Map.Entry<String, List<StockItem>> stockItemEntry : stockItemListMap.entrySet()) {
                List<StockItem> stockItemList = stockItemEntry.getValue();

                ExcelCell nameCell = new ExcelCell(2, col2Index + i, heightInPoints, stockItemEntry.getKey(), stockItemList.size(), 1);
                nameCell.setStyle(ExcelCellStyleHelper.createRowMergeCellStyle(excelWriter.getBook()));
                excelWriter.writeCell(nameCell);

                for (StockItem stockItem : stockItemList) {
                    excelWriter.writeRow(new ExcelRow(3, col2Index + i, heightInPoints, stockItem.getMaterialCode(), stockItem.getMaterialSpecification(),
                            stockItem.getCharacteristic(), stockItem.getQuantity(), stockItem.getUnitText()));
                    i++;
                }
            }

            col1Index += col1RowSpan;
        }

        excelWriter.toFile(HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, fileName + ".xlsx"));
    }

    private Map<String, Map<String, List<StockItem>>> data(Collection<Long> plantIds) {
        Map<Long, Category> categoryIdMap = categoryDAO.selectAll().stream().collect(Collectors.toMap(Category::getId, category -> category));

        String sql = "select mm_material.category_id, material_id, batch_id, quantity from inv_stock inner join mm_material where " +
                "mm_material.id = inv_stock.material_id AND mm_material.is_deleted = 0 AND inv_stock.plant_id IN (:plantIds)";
        List<StockItem> stockItemList = sharpService.query(sql, Params.builder(1).pv("plantIds", plantIds).build(), StockItem.class);
        materialService.fillMaterialDescription(stockItemList);
        batchService.fillCharacteristicValue(stockItemList);
        stockItemList.forEach(stockItem -> stockItem.setCategoryName(
//                dictService.getDictByTypeAndName("core_material_category", String.valueOf(stockItem.getCategoryId())).get().getLabel()
                categoryIdMap.get(stockItem.getCategoryId()).getName()
        ));

        Map<String, Map<String, List<StockItem>>> data = stockItemList.stream().collect(Collectors.groupingBy(stockItem -> categoryIdMap.get(stockItem.getCategoryId()).getOrderIndex() + "-" + stockItem.getCategoryName(),
                TreeMap::new,
                Collectors.groupingBy(stockItem -> stockItem.getMaterialText(), Collectors.toList())));
        return data;
    }
}

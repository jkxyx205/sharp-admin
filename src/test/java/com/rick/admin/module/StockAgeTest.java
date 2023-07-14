package com.rick.admin.module;

import com.rick.db.service.support.Params;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.*;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @author Rick.Xu
 * @date 2023/7/14 10:42
 */
@SpringBootTest
public class StockAgeTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(711313332417597440L)
                .code("stock_age")
                .tplName("tpl/list")
                .name("库龄分析")
                .additionalInfo(Params.builder(1).pv("formId", "695978675677433856").build())
                .reportAdviceName("stockAgeReportAdvice")
                .querySql("SELECT mm_material.id, mm_material.code, mm_material.name,characteristic,base_unit, IFNULL(stock.quantity, 0)  stock_quantity FROM mm_material left join (select material_id, sum(quantity) quantity from inv_stock group by material_id) stock on stock.material_id = mm_material.id where mm_material.id = :materialId AND mm_material.code = :materialCode")
                .queryFieldList(Arrays.asList(
                        new QueryField("materialCode", "物料", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称", true),
                        new ReportColumn("characteristic", "规格", false, null, Arrays.asList("characteristicConverter")),
                        new ReportColumn("base_unit", "基本单位", false, "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("stock_quantity", "库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("0", "<=7天").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("1", "7天-30天").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("2", "30天-90天").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("3", ">90天").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

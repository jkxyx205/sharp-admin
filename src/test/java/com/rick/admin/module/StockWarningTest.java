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
 * @date 2023/6/12 14:19
 */
@SpringBootTest
public class StockWarningTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(708404920146989056L)
                .code("stock_warning")
                .tplName("modules/inventory/warning")
//                .tplName("tpl/list")
//                .tplName("tpl/ajax_list")
                .name("安全库存预警")
                .additionalInfo(Params.builder(1).pv("formId", "695978675677433856").build())
                .reportAdviceName("stockWarningReportAdvice")
                .querySql("select id, code, name, `specification`, mm_material.category_id, ifnull(stock.quantity, 0) stock_quantity, `safety_stock_quantity`, `maximum_stock_quantity` from mm_material left join (select material_id, sum(quantity) quantity from inv_stock where plant_id = 719893335619162112 group by material_id) stock on stock.material_id = mm_material.id where ((safety_stock_quantity is not null and safety_stock_quantity > ifnull(stock.quantity, 0))  or (maximum_stock_quantity is not null and maximum_stock_quantity < ifnull(stock.quantity, 0))) AND mm_material.id = :material_id AND category_id = :categoryId")
                .queryFieldList(Arrays.asList(
                        new QueryField("categoryId", "分类", QueryField.Type.SELECT, "core_material_category")
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称", true),
                        new ReportColumn("specification", "规格", false, null, Arrays.asList("characteristicConverter")),
                        new ReportColumn("category_path", "分类", false),
                        new ReportColumn("stock_quantity", "材料库库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("safety_stock_quantity", "安全库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("maximum_stock_quantity", "最大库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

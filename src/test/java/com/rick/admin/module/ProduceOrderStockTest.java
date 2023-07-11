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
 * @date 2023/6/2 10:42
 */
@SpringBootTest
public class ProduceOrderStockTest {

    @Autowired
    private ReportService reportService;



    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(710331838161227776L)
                .code("produce_order_stock")
                .tplName("tpl/list")
                .name("生产库存")
                .additionalInfo(Params.builder(1).pv("formId", "695978675677433856").build())
                .reportAdviceName("produceOrderStockReportAdvice")
                .querySql("SELECT mm_material.id, mm_material.code, mm_material.name, characteristic, base_unit, IFNULL(stock.quantity, 0) stock_quantity, IFNULL(open.open_quantity, 0) open_quantity, po.quantity FROM mm_material left join (select material_id, sum(quantity) quantity from inv_stock group by material_id) stock on stock.material_id = mm_material.id inner join (select produce_bom_detail.material_id, sum(produce_order_item.quantity * produce_bom_detail.quantity) quantity from `produce_order` left join produce_order_item on produce_order.id = produce_order_item.`produce_order_id` inner join `produce_bom` on produce_bom.material_id = produce_order_item.material_id inner join produce_bom_detail on produce_bom_detail.bom_id = produce_bom.id where produce_order.`status` = 'PLANNING' group by produce_bom_detail.material_id) po on po.material_id = mm_material.id left join (select material_id, sum(pur_purchase_order_item.quantity - ifnull(receive.quantity, 0)) open_quantity from `pur_purchase_order_item` left join (select inv_document_item.`root_reference_code`, root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where exists(select 1 from `pur_purchase_order_item` where `is_complete` = 0 and inv_document_item.`root_reference_code` = pur_purchase_order_item.`purchase_order_code`) group by inv_document_item.`root_reference_code`, root_reference_item_id) receive on pur_purchase_order_item.purchase_order_code = receive.root_reference_code and pur_purchase_order_item.id = receive.root_reference_item_id where `is_complete` = 0 group by material_id) open on open.material_id = mm_material.id where mm_material.id = :materialId AND mm_material.code = :materialCode")
                .queryFieldList(Arrays.asList(
                        new QueryField("materialCode", "物料", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称", true),
                        new ReportColumn("characteristic", "规格", false, null, Arrays.asList("characteristicConverter")),
                        new ReportColumn("base_unit", "基本单位", false, "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("stock_quantity", "实际库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("open_quantity", "在途库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("quantity", "生产需求").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("diffQuantity", "差异").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT)
                ))
                .pageable(false)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

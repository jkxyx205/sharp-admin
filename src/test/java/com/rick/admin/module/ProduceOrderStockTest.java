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
                .name("生产库存预警")
                .additionalInfo(Params.builder(1).pv("formId", "695978675677433856").build())
                .reportAdviceName("produceOrderStockReportAdvice")
                .querySql("SELECT mm_material.id,\n" +
                        "       mm_material.code,\n" +
                        "       mm_material.name,\n" +
                        "       specification,\n" +
                        "       base_unit,\n" +
                        "       po.batch_id,\n" +
                        "       po.batch_code,\n" +
                        "       concat(mm_material.id, ifnull(batch_id, '')) materialIdBatchIdString,\n" +
                        "       IFNULL(stock.quantity, 0)     stock_quantity,\n" +
                        "       IFNULL(open.open_quantity, 0) open_quantity,\n" +
                        "       IFNULL(pdo_receive.quantity, 0) receive_quantity,\n" +
                        "       po.quantity FROM mm_material\n" +
                        "         inner join (select produce_order_item_detail.material_id, produce_order_item_detail.batch_id,produce_order_item_detail.batch_code,\n" +
                        "                            sum(produce_order_item.quantity * produce_order_item_detail.quantity) quantity\n" +
                        "                     from `produce_order`\n" +
                        "                              left join produce_order_item on produce_order.id = produce_order_item.`produce_order_id`\n" +
                        "                              inner join produce_order_item_detail on produce_order_item_detail.produce_order_item_id = produce_order_item.id join produce_order_item_schedule on produce_order_item_schedule.`produce_order_id` = produce_order.id AND produce_order_item_schedule.`produce_order_item_id` = produce_order_item.id AND produce_order_item_schedule.`status` = 'PRODUCING' WHERE produce_order.`status` = 'PRODUCING'\n" +
                        "                     group by produce_order_item_detail.material_id, produce_order_item_detail.batch_code) po on po.material_id = mm_material.id\n" +
                        "         left join (select material_id, batch_code, sum(quantity) quantity from inv_stock where plant_id = 719893335619162112 group by material_id, batch_code ) stock\n" +
                        "                   on stock.material_id = mm_material.id AND ifnull(stock.batch_code, '') = ifnull(po.batch_code, '')\n" +
                        "\t\t left join(select inv_document_item.material_id, inv_document_item.batch_code,\n" +
                        "                                               ABS(ifnull(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity), 0)) quantity\n" +
                        "                                        from inv_document_item where plant_id = '719893335619162112' AND reference_type='PP' AND exists (select 1 from produce_order_item_schedule where produce_order_item_schedule.`status` = 'PRODUCING' AND root_reference_code = produce_order_item_schedule.code)  group by inv_document_item.material_id, inv_document_item.batch_code) pdo_receive on pdo_receive.material_id = mm_material.id AND ifnull(pdo_receive.batch_code, '') = ifnull(po.batch_code, '')\n" +
                        "         left join (select material_id, batch_code,\n" +
                        "                           sum(pur_purchase_order_item.quantity - ifnull(receive.quantity, 0)) open_quantity\n" +
                        "                    from `pur_purchase_order_item`\n" +
                        "                             left join (select inv_document_item.`root_reference_code`,\n" +
                        "                                               root_reference_item_id,\n" +
                        "                                               ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity\n" +
                        "                                        from inv_document_item\n" +
                        "                                        where exists(select 1\n" +
                        "                                                     from `pur_purchase_order_item`\n" +
                        "                                                     where `is_complete` = 0\n" +
                        "                                                       and inv_document_item.`root_reference_code` =\n" +
                        "                                                           pur_purchase_order_item.`purchase_order_code`)\n" +
                        "                                        group by inv_document_item.`root_reference_code`,\n" +
                        "                                                 root_reference_item_id) receive\n" +
                        "                                       on pur_purchase_order_item.purchase_order_code = receive.root_reference_code and\n" +
                        "                                          pur_purchase_order_item.id = receive.root_reference_item_id\n" +
                        "                    where `is_complete` = 0\n" +
                        "                    group by material_id, pur_purchase_order_item.batch_code) open on open.material_id = mm_material.id AND ifnull(po.batch_code, '') = ifnull(open.batch_code, '') where IFNULL(stock.quantity, 0) + IFNULL(pdo_receive.quantity, 0)  < po.quantity AND mm_material.id = :materialId\n" +
                        "  AND mm_material.code = :materialCode")
                .queryFieldList(Arrays.asList(
                        new QueryField("materialCode", "物料", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称", true),
//                        new ReportColumn("specification", "规格", false, null, Arrays.asList("characteristicConverter")),
//                        new ReportColumn("characteristic", "特征值"),
                        new ReportColumn("specificationAndCharacteristic", "规格 & 特征值"),
                        new ReportColumn("base_unit", "基本单位", false, "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("stock_quantity", "材料库库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("open_quantity", "在途库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("receive_quantity", "已领料").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("quantity", "生产需求").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("diffQuantity", "待采购").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new HiddenReportColumn("batch_code"),
                        new HiddenReportColumn("batch_id"),
                        new HiddenReportColumn("needPurchase")
                ))
                .pageable(false)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

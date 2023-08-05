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
                .querySql("SELECT mm_material.id,\n" +
                        "       mm_material.code,\n" +
                        "       mm_material.name,\n" +
                        "       specification,\n" +
                        "       mm_characteristic_value.value color,\n" +
                        "       base_unit,\n" +
                        "       IFNULL(stock.quantity, 0)     stock_quantity,\n" +
                        "       IFNULL(open.open_quantity, 0) open_quantity,\n" +
                        "       po.quantity\n" +
                        "FROM mm_material\n" +
                        "         inner join (select produce_order_item_detail.material_id, produce_order_item_detail.batch_code,\n" +
                        "                            sum(produce_order_item.quantity * produce_order_item_detail.quantity) quantity\n" +
                        "                     from `produce_order`\n" +
                        "                              left join produce_order_item on produce_order.id = produce_order_item.`produce_order_id`\n" +
                        "                              inner join produce_order_item_detail on produce_order_item_detail.produce_order_item_id = produce_order_item.id\n" +
                        "                     where produce_order.`status` = 'PLANNING' AND produce_order_item_detail.is_complete = 0\n" +
                        "                     group by produce_order_item_detail.material_id, produce_order_item_detail.batch_code) po on po.material_id = mm_material.id\n" +
                        "         left join mm_profile on mm_profile.material_id = po.material_id AND mm_profile.batch_code = po.batch_code\n" +
                        "         left join mm_characteristic_value on reference_id = mm_profile.id\n" +
                        "         left join (select material_id, batch_code, sum(quantity) quantity from inv_stock group by material_id, batch_code) stock\n" +
                        "                   on stock.material_id = mm_material.id AND (stock.batch_code = po.batch_code or (po.batch_code is null AND stock.batch_code is null))\n" +
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
                        "                    group by material_id, pur_purchase_order_item.batch_code) open on open.material_id = mm_material.id AND (stock.batch_code = open.batch_code or (open.batch_code is null AND stock.batch_code is null))\n" +
                        "where mm_material.id = :materialId\n" +
                        "  AND mm_material.code = :materialCode")
                .queryFieldList(Arrays.asList(
                        new QueryField("materialCode", "物料", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称", true),
                        new ReportColumn("specification", "规格", false, null, Arrays.asList("characteristicConverter")),
                        new ReportColumn("color", "颜色"),
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

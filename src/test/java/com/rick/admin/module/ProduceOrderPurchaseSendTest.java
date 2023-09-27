package com.rick.admin.module;

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
public class ProduceOrderPurchaseSendTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(737912419505934336L)
                .code("produce_order_purchase_send")
                .tplName("tpl/list")
                .name("采购直发")
                .reportAdviceName("produceOrderPurchaseSendAdvice")
                .querySql("select pur_purchase_requisition_item.id, pur_purchase_requisition_item.material_id, pur_purchase_requisition_item.material_code, mm_material.name material_name,  mm_material.specification, pur_purchase_requisition_item.batch_id, pur_purchase_requisition_item.batch_code, pur_purchase_requisition_item.quantity, pur_purchase_requisition_item.unit, pur_purchase_requisition_item.`delivery_date`, pur_purchase_requisition_item.remark, pur_purchase_requisition_item.reference_id, produce_order_item.produce_order_code, partner_id\n" +
                        "from pur_purchase_requisition_item, `produce_order_item`, produce_order, mm_material\n" +
                        "where produce_order.id = produce_order_item.produce_order_id AND pur_purchase_requisition_item.reference_id = produce_order_item.id\n" +
                        "AND pur_purchase_requisition_item.is_complete = 0 AND mm_material.id = pur_purchase_requisition_item.material_id AND mm_material.material_code = :materialCode")
                .queryFieldList(Arrays.asList(
                        new QueryField("materialCode", "物料", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("material_code", "编号"),
                        new ReportColumn("material_name", "名称", true),
                        new ReportColumn("specificationAndCharacteristic", "规格 & 特征值"),
                        new ReportColumn("unit", "基本单位", false, "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("quantity", "采购数量").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("delivery_date", "交货日期", false),
                        new ReportColumn("remark", "备注", false),
                        new ReportColumn("produce_order_code", "销售订单", false),
                        new ReportColumn("partner_id", "客户", false, "core_partner", Arrays.asList("dictConverter")),
                        new HiddenReportColumn("batch_code"),
                        new HiddenReportColumn("batch_id"),
                        new HiddenReportColumn("material_id"),
                        new HiddenReportColumn("reference_id")
                ))
                .pageable(false)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

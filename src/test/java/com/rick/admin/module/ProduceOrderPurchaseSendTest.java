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
public class ProduceOrderPurchaseSendTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(737912419505934336L)
                .code("produce_order_purchase_send")
                .tplName("tpl/list")
                .name("采购申请")
                .reportAdviceName("produceOrderPurchaseSendAdvice")
                .additionalInfo(Params.builder(1).pv("formId", "695978675677433856").build())
                .querySql("SELECT pur_purchase_requisition_item.id,\n" +
                        "        pur_purchase_requisition_item.material_id,\n" +
                        "        pur_purchase_requisition_item.material_code,\n" +
                        "        pur_purchase_requisition_item.batch_id,\n" +
                        "        pur_purchase_requisition_item.batch_code,\n" +
                        "        pur_purchase_requisition_item.quantity,\n" +
                        "        pur_purchase_requisition_item.unit,\n" +
                        "        pur_purchase_requisition_item.`delivery_date`,\n" +
                        "        pur_purchase_requisition_item.remark,\n" +
                        "        pur_purchase_requisition_item.reference_id,\n" +
                        "        mm_material.name material_name,\n" +
                        "        mm_material.specification,\n" +
                        "        produce_order_code,\n" +
                        "        partner_id\n" +
                        "         FROM pur_purchase_requisition_item, mm_material,\n" +
                        "         (select distinct reference_id, produce_order.id produce_id, produce_order.code produce_order_code, partner_id  from  pur_purchase_requisition_item,produce_order_item, produce_order where pur_purchase_requisition_item.is_complete = 0\n" +
                        "AND produce_order.id = produce_order_item.produce_order_id\n" +
                        "AND (pur_purchase_requisition_item.reference_id = produce_order.id or pur_purchase_requisition_item.reference_id = produce_order_item.id)) t\n" +
                        "        where mm_material.id = pur_purchase_requisition_item.material_id\n" +
                        "        and t.reference_id = pur_purchase_requisition_item.reference_id AND pur_purchase_requisition_item.is_complete = 0 and pur_purchase_requisition_item.is_deleted = 0 and (mm_material.code LIKE :materialCode OR mm_material.name LIKE :materialCode OR mm_material.specification LIKE :materialCode) AND produce_order_code LIKE :produceOrderCode")
                .queryFieldList(Arrays.asList(
                        new QueryField("materialCode", "物料", QueryField.Type.TEXT).setPlaceholder("物料编号、名称搜索"),
                        new QueryField("produceOrderCode", "销售订单", QueryField.Type.TEXT)
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

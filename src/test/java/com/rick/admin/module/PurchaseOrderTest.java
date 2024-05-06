package com.rick.admin.module;

import com.rick.report.core.entity.Report;
import com.rick.report.core.model.HiddenReportColumn;
import com.rick.report.core.model.QueryField;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.SordEnum;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Rick.Xu
 * @date 2023/6/12 14:19
 */
@SpringBootTest
public class PurchaseOrderTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(702566176051462144L)
                .code("pur_purchase_order")
                .tplName("modules/purchase/list")
                .name("订单列表")
//                .reportAdviceName("inventoryDocumentReportAdvice")
                .querySql("select id, code, partner_id, plant_id, status, remark, create_by, create_time from pur_purchase_order where code LIKE :code and status = :status and partner_id = :partner_id and create_by = :create_by and create_time >= :create_time0 and create_time <= :create_time1")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "订单号", QueryField.Type.TEXT),
                        new QueryField("partner_id", "供应商", QueryField.Type.SELECT, "core_partner_vendor"),
                        new QueryField("status", "状态", QueryField.Type.SELECT, "purchase_order_status"),
//                        new QueryField("create_by", "创建人", QueryField.Type.SELECT, "sys_user"),
                        new QueryField("create_time", "创建时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "订单号").setColumnWidth(130),
                        new ReportColumn("partner_id", "供应商", false, "core_partner", Arrays.asList("dictConverter")),
                        new ReportColumn("plant_id", "入库库房", false, "core_plant", Arrays.asList("dictConverter")),
//                        new ReportColumn("delivery_date", "交货日期", true,null, Arrays.asList("sqlDateConverter")).setColumnWidth(120),
                        new ReportColumn("status", "状态", false, "purchase_order_status", Arrays.asList("dictConverter")).setColumnWidth(100),
                        new ReportColumn("remark", "备注"),
                        new ReportColumn("create_by", "创建人", false, "sys_user", Arrays.asList("dictConverter")).setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间", false,null, Arrays.asList("localDateTimeConverter")).setColumnWidth(180)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.DESC)
                .build());
    }

    @Test
    public void testPurchaseDetail() {
        reportService.saveOrUpdate(Report.builder()
                .id(816284792017412096L)
                .code("pur_purchase_order_detail")
                .tplName("modules/purchase/list-detail")
                .name("采购明细列表")
                .reportAdviceName("listDetailReportAdvice")
                .additionalInfo(new HashMap<>())
                .summaryColumnNames("total_price")
                .querySql("select pur_purchase_order_item.id, pur_purchase_order.partner_id, pur_purchase_order.id order_id, pur_purchase_order.code, pur_purchase_order_item.material_id, pur_purchase_order_item.material_code,\n" +
                        "pur_purchase_order_item.batch_id, pur_purchase_order_item.batch_code, pur_purchase_order_item.quantity, pur_purchase_order_item.unit, pur_purchase_order_item.unit_price, (pur_purchase_order_item.quantity * pur_purchase_order_item.unit_price) total_price, pur_purchase_order.create_time, pur_purchase_order_item.delivery_date, (pur_purchase_order_item.is_complete + 0) is_complete from pur_purchase_order_item, pur_purchase_order where pur_purchase_order_item.purchase_order_code = pur_purchase_order.code and " +
                        "code = :code and partner_id = :partner_id and material_id = :material_id and material_code = :material_code and batch_id = :batch_id and is_complete = :is_complete and pur_purchase_order_item.create_time >= :create_time0 and pur_purchase_order_item.create_time <= :create_time1")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "订单号", QueryField.Type.TEXT),
                        new QueryField("partner_id", "供应商", QueryField.Type.SELECT, "core_partner_vendor"),
                        new QueryField("is_complete", "完成收货", QueryField.Type.SELECT, "bol"),
                        new QueryField("create_time", "订单时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new HiddenReportColumn("order_id"),
                        new HiddenReportColumn("material_id"),
                        new ReportColumn("code", "订单号").setColumnWidth(130),
                        new ReportColumn("partner_id", "供应商", false, "core_partner", Arrays.asList("dictConverter")),
                        new ReportColumn("material_code", "物料编号"),
                        new ReportColumn("materialName", "物料名称"),
                        new ReportColumn("specificationAndCharacteristic", "规格 & 特征值"),
                        new ReportColumn("quantity", "数量").setType(ReportColumn.TypeEnum.NUMERIC),
                        new ReportColumn("unit", "单位", false, "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("unit_price", "含税单价(元)").setType(ReportColumn.TypeEnum.DECIMAL),
                        new ReportColumn("total_price", "含税总计(元)").setType(ReportColumn.TypeEnum.DECIMAL),
                        new ReportColumn("delivery_date", "交货日期", false,null, Arrays.asList("sqlDateConverter")).setColumnWidth(120),
                        new ReportColumn("create_time", "采购时间", false,null, Arrays.asList("localDateTimeConverter")).setColumnWidth(180),
                        new ReportColumn("is_complete", "收货状态", false, "bol", Arrays.asList("dictConverter")).setColumnWidth(50),
                        new ReportColumn("goodsReceiptQuantity", "已收货数量"),
                        new ReportColumn("openQuantity", "待收货数量")
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

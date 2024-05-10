package com.rick.admin.module;

import com.rick.db.service.support.Params;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.*;
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
public class ProduceOrderTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(710221635797413888L)
                .code("produce_order")
                .tplName("tpl/list")
                .name("销售订单")
                .reportAdviceName("produceOrderReportAdvice")
                // 必须通过指定 endpoint 完成 表单的CUD操作
                .additionalInfo(Params.builder(1).pv("endpoint", "produce_orders").build())
                .querySql("select id, code, partner_id, status, remark, create_by, create_time from produce_order where code LIKE :code and create_by = :create_by and create_time >= :create_time0 and create_time <= :create_time1 AND status = :status AND partner_id = :partner_id")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "销售单号", QueryField.Type.TEXT),
                        new QueryField("partner_id", "客户", QueryField.Type.SELECT, "core_partner_customer"),
                        new QueryField("status", "状态", QueryField.Type.SELECT, "produce_order_status"),
//                        new QueryField("create_by", "创建人", QueryField.Type.SELECT, "sys_user"),
                        new QueryField("create_time", "创建时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "销售单号").setColumnWidth(180),
                        new ReportColumn("partner_id", "客户", false, "core_partner", Arrays.asList("dictConverter")),
                        new ReportColumn("status", "状态", false, "produce_order_status", Arrays.asList("dictConverter")).setColumnWidth(100),
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
    public void testScheduleReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(736036369939337216L)
                .code("produce_order_item_schedule")
                .tplName("modules/produce/produce_schedule")
                .name("生产计划")
                .reportAdviceName("produceScheduleReportAdvice")
                .querySql("select produce_order_item_schedule.id, produce_order_item_schedule.code, start_date startDate, produce_order_item.material_id materialId, produce_order_item.material_code materialCode, mm_material.name, produce_order_item.specification, produce_order_item.batch_id, produce_order_item.batch_code, produce_order_item_schedule.quantity, produce_order_item_schedule.unit, produce_order_item_schedule.status, concat(produce_order.remark, produce_order_item.remark, produce_order_item_schedule.remark) remark, produce_order.code produceOrderCode, produce_order.partner_id partnerId from produce_order_item_schedule\n" +
                        "join produce_order_item on produce_order_item.id = produce_order_item_schedule.`produce_order_item_id`\n" +
                        "left join mm_material on mm_material.id = produce_order_item.material_id\n" +
                        "left join produce_order on produce_order.id = produce_order_item.`produce_order_id`" +
                        "WHERE produce_order.`status` <> 'PLANNING' AND produce_order.`is_deleted` = 0 AND produce_order.code LIKE :produceOrderCode AND produce_order_item_schedule.code LIKE :code AND produce_order_item_schedule.status = :status AND start_date >= :startDate0 and start_date <= :startDate1 order by start_date desc")
                .queryFieldList(Arrays.asList(
//                        new QueryField("code", "生产单", QueryField.Type.TEXT),
//                        new QueryField("partner_id", "客户", QueryField.Type.SELECT, "core_partner"),
                        new QueryField("status", "状态", QueryField.Type.SELECT, "produce_schedule_status"),
                        new QueryField("code", "生产单号", QueryField.Type.TEXT),
                        new QueryField("startDate", "生产日期", QueryField.Type.DATE_RANGE),
                        new QueryField("produceOrderCode", "销售单号", QueryField.Type.TEXT)
//                        new QueryField("create_by", "创建人", QueryField.Type.SELECT, "sys_user"),
//                        new QueryField("create_time", "创建时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new HiddenReportColumn("materialId"),
                        new ReportColumn("startDate", "生产日期").setColumnWidth(100),
                        new ReportColumn("materialCode", "物料").setColumnWidth(80),
                        new ReportColumn("name", "名称").setTooltip(true),
                        new ReportColumn("specificationAndCharacteristic", "规格 & 特征值"),
                        new ReportColumn("quantity", "生产数量").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT).setColumnWidth(80),
                        new ReportColumn("unit", "单位", false, "unit", Arrays.asList("dictConverter")).setColumnWidth(60),
                        new ReportColumn("status", "状态", false, "produce_order_status", Arrays.asList("dictConverter")).setColumnWidth(80),
                        new ReportColumn("remark", "备注"),
                        new ReportColumn("code", "生产单号").setColumnWidth(160),
                        new ReportColumn("produceOrderCode", "销售单号").setColumnWidth(160),
                        new ReportColumn("partnerId", "客户", false, "core_partner_customer", Arrays.asList("dictConverter"))
                ))
                .pageable(true)
                .build());
    }

    @Test
    public void testProduceDetail() {
        reportService.saveOrUpdate(Report.builder()
                .id(820353148383137792L)
                .code("produce_order_detail")
                .tplName("modules/produce/list-detail")
                .name("销售明细列表")
                .reportAdviceName("produceListDetailReportAdvice")
                .additionalInfo(new HashMap<>())
                .summaryColumnNames("total_price")
                .querySql("select id, partner_id, order_id, code, material_id, material_code, batch_id, batch_code, quantity, unit_price, total_price, create_time, delivery_date, is_complete from (select produce_order_item.id, produce_order.partner_id, produce_order.id order_id, produce_order.code, produce_order_item.material_id, produce_order_item.material_code, produce_order_item.batch_id, produce_order_item.batch_code, produce_order_item.quantity, produce_order_item.unit, ifnull(produce_order_item.unit_price, 0) unit_price, (produce_order_item.quantity * ifnull(produce_order_item.unit_price, 0)) total_price, produce_order.create_time, produce_order_item.delivery_date, (produce_order_item.is_complete + 0) is_complete from produce_order_item, produce_order where produce_order_item.produce_order_code = produce_order.code and \n" +
                        "code = :code and partner_id = :partner_id and material_id = :material_id and material_code = :material_code and batch_id = :batch_id and is_complete = :is_complete and produce_order_item.create_time >= :create_time0 and produce_order_item.create_time <= :create_time1) t")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "订单号", QueryField.Type.TEXT),
                        new QueryField("partner_id", "供应商", QueryField.Type.SELECT, "core_partner_customer"),
                        new QueryField("is_complete", "完成收货", QueryField.Type.SELECT, "bol"),
                        new QueryField("create_time", "订单时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new HiddenReportColumn("order_id"),
                        new HiddenReportColumn("material_id"),
                        new ReportColumn("code", "订单号").setColumnWidth(130),
                        new ReportColumn("partner_id", "客户", false, "core_partner", Arrays.asList("dictConverter")),
                        new ReportColumn("material_code", "物料编号"),
                        new ReportColumn("materialName", "物料名称"),
                        new ReportColumn("specificationAndCharacteristic", "规格 & 特征值"),
                        new ReportColumn("quantity", "数量").setType(ReportColumn.TypeEnum.NUMERIC),
                        new ReportColumn("unit", "单位", false, "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("unit_price", "含税单价(元)").setType(ReportColumn.TypeEnum.DECIMAL),
                        new ReportColumn("total_price", "含税总计(元)").setType(ReportColumn.TypeEnum.DECIMAL),
                        new ReportColumn("delivery_date", "交货日期", false,null, Arrays.asList("sqlDateConverter")).setColumnWidth(120),
                        new ReportColumn("create_time", "销售时间", false,null, Arrays.asList("localDateTimeConverter")).setColumnWidth(180),
                        new ReportColumn("is_complete", "收货状态", false, "bol", Arrays.asList("dictConverter")).setColumnWidth(50),
                        new ReportColumn("goodsIssueQuantity", "已收货数量"),
                        new ReportColumn("openQuantity", "待收货数量")
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

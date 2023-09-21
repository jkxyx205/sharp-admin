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
//                .reportAdviceName("inventoryDocumentReportAdvice")
                // 必须通过指定 endpoint 完成 表单的CUD操作
                .additionalInfo(Params.builder(1).pv("endpoint", "produce_orders").build())
                .querySql("select id, code, partner_id, status, remark, create_by, create_time from produce_order where code = :code and create_by = :create_by and create_time >= :create_time0 and create_time <= :create_time1 AND status = :status")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "销售单号", QueryField.Type.TEXT),
                        new QueryField("partner_id", "客户", QueryField.Type.SELECT, "core_partner"),
                        new QueryField("status", "状态", QueryField.Type.SELECT, "produce_order_status"),
                        new QueryField("create_by", "创建人", QueryField.Type.SELECT, "sys_user"),
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
                .tplName("modules/produce_schedule")
                .name("生产计划")
                .reportAdviceName("produceScheduleReportAdvice")
                .querySql("select produce_order_item_schedule.id, produce_order_item_schedule.code, start_date startDate, produce_order_item.material_id materialId, produce_order_item.material_code materialCode, mm_material.name, produce_order_item.specification, produce_order_item.batch_id, produce_order_item.batch_code, produce_order_item_schedule.quantity, produce_order_item_schedule.unit, produce_order_item_schedule.status, concat(produce_order.remark, produce_order_item.remark, produce_order_item_schedule.remark) remark, produce_order.code produceOrderCode from produce_order_item_schedule\n" +
                        "join produce_order_item on produce_order_item.id = produce_order_item_schedule.`produce_order_item_id`\n" +
                        "left join mm_material on mm_material.id = produce_order_item.material_id\n" +
                        "left join produce_order on produce_order.id = produce_order_item.`produce_order_id`" +
                        "WHERE produce_order.`status` <> 'PLANNING' AND produce_order.`is_deleted` = 0 AND produce_order.code = :produceOrderCode AND produce_order_item_schedule.code = :code AND produce_order_item_schedule.status = :status AND start_date >= :startDate0 and start_date <= :startDate1 order by start_date asc")
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
                        new ReportColumn("produceOrderCode", "销售单号").setColumnWidth(160)
                ))
                .pageable(true)
                .build());
    }
}

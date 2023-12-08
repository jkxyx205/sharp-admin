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
                        new QueryField("create_by", "创建人", QueryField.Type.SELECT, "sys_user"),
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
}

package com.rick.admin.module;

import com.rick.db.service.support.Params;
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
public class ProduceOrderTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(710221635797413888L)
                .code("produce_order")
                .tplName("tpl/list")
                .name("生产单")
//                .reportAdviceName("inventoryDocumentReportAdvice")
                // 必须通过指定 endpoint 完成 表单的CUD操作
                .additionalInfo(Params.builder(1).pv("endpoint", "produce_orders").build())
                .querySql("select id, code, status, remark, create_by, create_time from produce_order where code = :code and create_by = :create_by and create_time >= :create_time0 and create_time <= :create_time1 AND status = :status")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "生产单", QueryField.Type.TEXT),
                        new QueryField("status", "状态", QueryField.Type.SELECT, "produce_order_status"),
                        new QueryField("create_by", "创建人", QueryField.Type.SELECT, "sys_user"),
                        new QueryField("create_time", "创建时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "生产单"),
                        new ReportColumn("status", "状态", false, "produce_order_status", Arrays.asList("dictConverter")),
                        new ReportColumn("remark", "备注"),
                        new ReportColumn("create_by", "创建人", false, "sys_user", Arrays.asList("dictConverter")),
                        new ReportColumn("create_time", "创建时间", false,null, Arrays.asList("localDateTimeConverter"))
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.DESC)
                .build());
    }
}

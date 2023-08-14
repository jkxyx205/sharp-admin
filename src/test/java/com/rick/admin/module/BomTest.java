package com.rick.admin.module;

import com.rick.db.service.support.Params;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.HiddenReportColumn;
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
public class BomTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(721874903405432832L)
                .code("bom_list")
                .tplName("tpl/list")
                .name("物料清单")
                .additionalInfo(Params.builder(1).pv("endpoint", "produce_bom").build())
                .querySql("select id, code, description, create_time from produce_bom_template")
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "bom"),
                        new ReportColumn("description", "描述"),
                        new ReportColumn("create_time", "创建时间", false,null, Arrays.asList("localDateTimeConverter")).setColumnWidth(160)
                ))
                .pageable(false)
                .sidx("create_time")
                .sord(SordEnum.ASC)
                .build());
    }
}

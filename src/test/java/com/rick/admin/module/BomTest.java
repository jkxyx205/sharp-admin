package com.rick.admin.module;

import com.rick.admin.module.produce.service.BomService;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.core.EntityDAOSupport;
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
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author Rick.Xu
 * @date 2023/6/12 14:19
 */
@SpringBootTest
public class BomTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private EntityDAOSupport entityDAOSupport;

    /**
     * entity 参与包扫描，可以注入使用
     */
//    @Autowired(required = false)
//    @Qualifier("bomDAO")
//    private EntityDAO entityDAO;

    @Resource
    private EntityDAO bomDAO;

    @Autowired
    private BomService bomService;

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void testInsertBom() {
//        Bom bom = Bom.builder()
//                .materialId(697175396411510784L)
//                .itemList(Arrays.asList(
//                        Bom.Item.builder()
//                                .materialId(697175443165417472L)
//                                .quantity(BigDecimal.TEN)
//                                .unit("KG")
//                                .remark("remark")
//                                .build()
//                ))
//                .build();

        // 不参与包扫描也可以使用
//        entityDAOSupport.getEntityDAO(bom.getClass()).insert(bom);

//        entityDAO.insert(bom);

//        applicationContext.getBean("bomDAO", EntityDAO.class)
//                .insert(bom);

//        bomDAO.insert(bom);
//        bomService.saveOrUpdate(bom);
    }


    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(699659248728047616L)
                .code("inv_document_item")
                .tplName("modules/inventory/list")
//                .tplName("tpl/list")
//                .tplName("tpl/ajax_list")
                .name("物料凭证")
                .additionalInfo(Params.builder(1).pv("formId", "695978675677433856").build())
                .reportAdviceName("inventoryDocumentReportAdvice")
                .querySql("select `id`, `inventory_document_id`, `inventory_document_code`,`type`, `reference_type`, `reference_code`, `reference_item_id`, `root_reference_code`, `movement_type`, `plant_id`, `material_id`, `quantity`, `unit`, `remark`,  `create_by`, `create_time` from inv_document_item where inventory_document_code = :inventory_document_code AND material_id = :material_id AND movement_type = :movement_type AND root_reference_code = :root_reference_code and root_reference_item_id = :root_reference_item_id")
                .queryFieldList(Arrays.asList(
                        new QueryField("inventory_document_code", "物料凭证", QueryField.Type.TEXT),
//                        new QueryField("material_id", "物料", QueryField.Type.TEXT),
                        new QueryField("root_reference_code", "参考凭证", QueryField.Type.TEXT),
                        new QueryField("movement_type", "移动类型", QueryField.Type.SELECT, "movement_type")
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new HiddenReportColumn("inventory_document_id"),
                        new ReportColumn("inventory_document_code", "物料凭证"),
                        new ReportColumn("type", "场景", false, "inv_type", Arrays.asList("dictConverter")),
                        new ReportColumn("reference_type", "参考类型", false, "reference_type", Arrays.asList("dictConverter")),
                        new ReportColumn("root_reference_code", "参考凭证"),
                        new HiddenReportColumn("reference_item_id"),
                        new ReportColumn("movement_type", "移动类型", false, "movement_type", Arrays.asList("dictConverter")),
                        new ReportColumn("plant_id", "库房", false, "core_plant", Arrays.asList("dictConverter")),
                        new HiddenReportColumn("material_id"),
                        new ReportColumn("material_code", "物料编号"),
                        new ReportColumn("material_text", "物料描述"),
                        new ReportColumn("quantity", "数量"),
                        new ReportColumn("unit", "单位", false, "unit", Arrays.asList("dictConverter")),
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

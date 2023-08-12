package com.rick.admin.module;

import com.google.common.collect.Lists;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.common.util.JsonUtils;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.CpnConfigurerService;
import com.rick.formflow.form.service.FormCpnService;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.valid.CustomizeRegex;
import com.rick.formflow.form.valid.Length;
import com.rick.formflow.form.valid.Required;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.*;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick.Xu
 * @date 2023/6/2 10:42
 */
@SpringBootTest
public class MaterialTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private FormCpnService formCpnService;

    @Autowired
    private CpnConfigurerService cpnConfigurerService;

    @Autowired
    private FormService formService;

    @Autowired
    private MaterialDAO materialDAO;

    @Autowired
    private MaterialService materialService;

    @Test
    public void testSaveOrUpdate() {
        // 分类
        materialService.saveOrUpdate(Material.builder()
                .code("PC")
                .name("电脑")
                .categoryId(0L)
                .classificationList(Arrays.asList(
                        Classification.builder()
                                .classificationCode("COLOR")
                                .characteristicValueList(Arrays.asList(
                                        CharacteristicValue.builder()
                                                .characteristicCode("COLOR")
                                                .val("亚黑")
                                                .build()
                                ))
                                .build()))
                .build());
    }

    @Test
    public void testFind() {
        Material pc = materialService.findByCode("PC").get();
        System.out.println(JsonUtils.toJson(pc));
        assertThat(pc.getClassificationList().get(0).getClassification().getCharacteristicValue().get(0).getValue()).isEqualTo("亚黑");
    }

    @Test
    public void testForm() {
        // 设计控件
        List<CpnConfigurer> cpnConfigurerList = createCpnConfigurerList();
        cpnConfigurerService.saveOrUpdate(cpnConfigurerList);
        // 创建表
        Form form = formService.saveOrUpdate(Form.builder()
                .id(695978675677433856L)
                .code("mm_material")
                .tableName("mm_material")
                .tplName("modules/material")
                .formAdviceName("materialFormAdvice")
                .name("物料")
                .repositoryName("materialDAO")
                .storageStrategy(Form.StorageStrategyEnum.CREATE_TABLE)
                .additionalInfo(Params.builder(1)
                        .pv("label-col", 3)
//                        .pv("pane-list", Arrays.asList("一般数据", "量纲", "规格", "附件", "其他"))
                        .pv("pane-list", Arrays.asList("一般数据", "量纲", "规格","需求计划", "附件",
                                Params.builder(2).pv("label", "其他").pv("hideIfAdd", true).build()))
                        .build())
                .build());
        // 关联关系
        formCpnService.saveOrUpdateByConfigIds(form.getId(), cpnConfigurerList.stream().map(CpnConfigurer::getId).collect(Collectors.toList()));
    }

    private List<CpnConfigurer> createCpnConfigurerList() {
        // 文本校验器
        List<Validator> textValidatorList = Lists.newArrayListWithExpectedSize(2);
        textValidatorList.add(new Length(16));

        // 文本校验器
        List<Validator> textRequiredValidatorList = Lists.newArrayListWithExpectedSize(2);
        textRequiredValidatorList.add(new Required(true));
        textRequiredValidatorList.add(new Length(32));

        // code
        CustomizeRegex codeRegex = new CustomizeRegex("^[0-9a-zA-Z_\\/%\\-]{1,}$", "CODE只能包含数字、字母、下划线、中划线");
        List<Validator> codeRegexValidatorList = Lists.newArrayList(textValidatorList);

        // long text
        List<Validator> longTextValidatorList = Lists.newArrayListWithExpectedSize(1);
        longTextValidatorList.add(new Length(512));

        codeRegexValidatorList.add(codeRegex);

        CpnConfigurer codeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("code")
                .label("编码")
                .placeholder("请输入编码")
                .validatorList(codeRegexValidatorList)
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer materialTypeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("materialType")
                .label("物料类型")
                .datasource("material_type")
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer nameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("名称")
                .validatorList(textRequiredValidatorList)
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer categoryCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("categoryId")
                .label("分类")
//                .datasource("core_material_category")
                .datasource("material_category_select_sql")
                .validatorList(Arrays.asList(new Required(true)))
                .additionalInfo(Params.builder(1).pv("pane-index", "1")
                        // 分组显示
                        .pv("group", "true").build())
                .build();

        CpnConfigurer baseUnitCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("baseUnit")
                .label("基本单位")
                .datasource("unit")
                .validatorList(Arrays.asList(new Required(true)))
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer standardPriceCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.NUMBER_TEXT)
                .name("standardPrice")
                .label("含税单价(元)")
                .placeholder("")
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer batchManagementCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .name("batchManagement")
                .label("批次管理")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("1", "")))
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer serialManagementCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .name("serialManagement")
                .label("序列号管理")
                .options(Arrays.asList(new CpnConfigurer.CpnOption("1", "")))
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer remarkCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXTAREA)
                .name("remark")
                .label("备注")
                .validatorList(longTextValidatorList)
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer grossWeightCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.INTEGER_NUMBER)
                .name("grossWeight")
                .label("毛重")
                .additionalInfo(Params.builder(1).pv("pane-index", "2").build())
                .build();

        CpnConfigurer netWeightCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.INTEGER_NUMBER)
                .name("netWeight")
                .label("净重")
                .additionalInfo(Params.builder(1).pv("pane-index", "2").build())
                .build();

        CpnConfigurer weightUnitCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("weightUnit")
                .defaultValue("G")
                .datasource("unit")
                .label("重量单位")
                .disabled(true)
                .additionalInfo(Params.builder(1).pv("pane-index", "2").build())
                .build();

        CpnConfigurer volumeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.INTEGER_NUMBER)
                .name("volume")
                .label("体积")
                .additionalInfo(Params.builder(1).pv("pane-index", "2").build())
                .build();

        CpnConfigurer volumeUnitCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("volumeUnit")
                .datasource("unit")
                .defaultValue("CCM")
                .label("体积单位")
                .disabled(true)
                .additionalInfo(Params.builder(1).pv("pane-index", "2").build())
                .build();

        CpnConfigurer sizeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("size")
                .label("长／宽／高")
                .additionalInfo(Params.builder(1).pv("pane-index", "2").build())
                .build();

        CpnConfigurer characteristicKeyCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("characteristic_key")
                .label("特征")
                .additionalInfo(Params.builder(1).pv("pane-index", "3").build())
                .build();

        CpnConfigurer characteristicValueCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("characteristic_value")
                .label("值")
                .additionalInfo(Params.builder(1).pv("pane-index", "3").build())
                .build();

        CpnConfigurer characteristicCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TABLE)
                .name("specification")
                .label("规格")
                .additionalInfo(Params.builder()
                        .pv("columns", Arrays.asList(characteristicKeyCpn, characteristicValueCpn))
                        .pv("pane-index", "3")
                        .build())
                .build();

        CpnConfigurer safetyStockQuantityCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.NUMBER_TEXT)
                .name("mrp.safetyStockQuantity")
                .label("安全库存")
                .additionalInfo(Params.builder(1).pv("pane-index", "4").build())
                .build();

        CpnConfigurer maximumStockQuantityCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.NUMBER_TEXT)
                .name("mrp.maximumStockQuantity")
                .label("最大库存")
                .additionalInfo(Params.builder(1).pv("pane-index", "4").build())
                .build();

        CpnConfigurer attachmentCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.FILE)
                .name("attachment")
                .label("附件")
                .additionalInfo(Params.builder(1).pv("pane-index", "5").build())
                .build();

        // 其他信息
        CpnConfigurer stockQuantityCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.LABEL)
                .name("stockQuantity")
                .label("库存")
                .additionalInfo(Params.builder(1).pv("pane-index", "6").build())
                .build();

        CpnConfigurer stockQuantityPriceCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.LABEL)
                .name("stockQuantityPrice")
                .label("库存金额")
                .additionalInfo(Params.builder(1).pv("pane-index", "6").build())
                .build();

        CpnConfigurer createNameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.LABEL)
                .name("createBy")
                .datasource("sys_user")
                .label("创建人")
                .additionalInfo(Params.builder(1).pv("pane-index", "6").build())
                .build();

        CpnConfigurer createTimeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.LABEL)
                .name("createTime")
                .label("创建时间")
                .cpnValueConverterName("dateTimeToStringConverter")
                .additionalInfo(Params.builder(1).pv("pane-index", "6").build())
                .build();

        CpnConfigurer updateNameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.LABEL)
                .name("updateBy")
                .label("更新人")
                .datasource("sys_user")
                .additionalInfo(Params.builder(1).pv("pane-index", "6").build())
                .build();

        CpnConfigurer updateTimeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.LABEL)
                .name("updateTime")
                .label("更新时间")
                .cpnValueConverterName("dateTimeToStringConverter")
                .additionalInfo(Params.builder(1).pv("pane-index", "6").build())
                .build();


        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(codeCpn, materialTypeCpn, nameCpn, categoryCpn, baseUnitCpn, standardPriceCpn, batchManagementCpn, serialManagementCpn,
                remarkCpn, grossWeightCpn, netWeightCpn, weightUnitCpn, volumeCpn, volumeUnitCpn, sizeCpn, characteristicCpn, safetyStockQuantityCpn, maximumStockQuantityCpn, attachmentCpn,
                stockQuantityCpn, stockQuantityPriceCpn, createNameCpn, createTimeCpn, updateNameCpn, updateTimeCpn);
        return cpnConfigurerList;
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(695981455636959232L)
                .code("mm_material")
//                .tplName("tpl/list")
                .tplName("tpl/ajax_list")
                .name("物料")
                .additionalInfo(Params.builder(1).pv("formId", "695978675677433856").build())
                .reportAdviceName("materialReportAdvice")
//                .querySql("SELECT mm_material.id, mm_material.code, mm_material.name, specification, case when attachment is null or length(attachment) <= 2 then '无' else '有' end  attachment, material_type, mm_material.category_id, base_unit, standard_price, sys_user.name create_name,DATE_FORMAT(mm_material.create_time, '%Y-%m-%d %H:%i:%s') create_time FROM mm_material left join sys_user on sys_user.id = mm_material.create_by WHERE mm_material.code = :code AND mm_material.name like :name AND material_type = :materialType AND category_id = :categoryId AND mm_material.is_deleted = 0")
                .querySql("SELECT mm_material.id, mm_material.code, mm_material.name, specification, color, case when attachment is null or length(attachment) <= 2 then '无' else '有' end  attachment, material_type, mm_material.category_id, base_unit, standard_price, batch_management batchManagement, sys_user.name create_name,DATE_FORMAT(mm_material.create_time, '%Y-%m-%d %H:%i:%s') create_time,stock.quantity stock_quantity, standard_price * stock.quantity stock_quantity_standard_price FROM mm_material left join sys_user on sys_user.id = mm_material.create_by left join (select stock.*, characteristic.color from (\n" +
                        " select material_id, batch_id, sum(quantity) quantity from inv_stock group by material_id, batch_id) stock left join (  select mm_profile.material_id, mm_profile.batch_id, mm_characteristic_value.value color from mm_profile, mm_characteristic_value where category = 'BATCH' AND mm_profile.id = mm_characteristic_value.`reference_id`) characteristic on characteristic.material_id = stock.material_id AND characteristic.batch_id = stock.batch_id ) stock on stock.material_id = mm_material.id\n WHERE mm_material.code = :code AND mm_material.name like :name AND material_type = :materialType AND category_id = :categoryId AND mm_material.is_deleted = 0")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "编号", QueryField.Type.TEXT),
                        new QueryField("name", "名称", QueryField.Type.TEXT),
                        new QueryField("materialType", "类型", QueryField.Type.SELECT, "material_type"),
//                        new QueryField("categoryId", "分类", QueryField.Type.SELECT, "core_material_category"),
                        new QueryField("categoryId", "分类", QueryField.Type.GROUP_SELECT, "material_category_select_sql")
//                        new QueryField("categoryId", "分类", QueryField.Type.SELECT, "category_path"),
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称", true),
                        new ReportColumn("specification", "规格", false, null, Arrays.asList("characteristicConverter")),
                        new ReportColumn("color", "颜色"),
                        new ReportColumn("material_type", "类型", false, "material_type", Arrays.asList("dictConverter")),
//                        new ReportColumn("category_id", "分类", false, "core_material_category", Arrays.asList("dictConverter")),
                        new ReportColumn("category_path", "分类", false),
                        new ReportColumn("batchManagement", "批次物料", false, null, Arrays.asList("boolConverter")),
                        new ReportColumn("base_unit", "基本单位", false, "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("attachment", "附件"),
                        new ReportColumn("standard_price", "标准价格(元)").setType(ReportColumn.TypeEnum.DECIMAL).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("stock_quantity", "库存").setType(ReportColumn.TypeEnum.NUMERIC).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("stock_quantity_standard_price", "库存金额(元)").setType(ReportColumn.TypeEnum.DECIMAL).setAlign(AlignEnum.RIGHT),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(180).setAlign(AlignEnum.CENTER)
                ))
                .pageable(true)
                .sidx("id")
                .summaryColumnNames("stock_quantity_standard_price")
                .sord(SordEnum.ASC)
                .build());
    }

    @Test
    public void testSearchReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(697147523487240192L)
                .code("mm_material_search")
                .tplName("tpl/query_list")
                .name("物料查询")
                .querySql("SELECT cast(mm_material.id as char(20)) id, mm_material.code, mm_material.name, specification, material_type, mm_material.category_id, mm_material.category_id as categoryText, base_unit, base_unit as base_unit_name, standard_price unitPrice, batch_management batchManagement FROM mm_material WHERE mm_material.code = :code AND (mm_material.name like :keywords or specification like :keywords or mm_material.code like :keywords) AND material_type = :materialType AND category_id = :categoryId AND mm_material.is_deleted = 0 AND id = :id AND id IN (:ids)")
                .queryFieldList(Arrays.asList(
//                        new QueryField("code", "编号", QueryField.Type.TEXT),
                        new QueryField("keywords", "关键字", QueryField.Type.TEXT),
//                        new QueryField("materialType", "类型", QueryField.Type.SELECT, "material_type"),
                        new QueryField("categoryId", "分类", QueryField.Type.GROUP_SELECT, "material_category_select_sql")
//                        new QueryField("categoryId", "分类", QueryField.Type.MULTIPLE_SELECT, "core_material_category")
//                        new QueryField("categoryId", "分类", QueryField.Type.SELECT, "core_material_category")

//                        new QueryField("categoryId", "分类", QueryField.Type.SELECT, "category_path")

                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new HiddenReportColumn("unitPrice"),
                        new HiddenReportColumn("category_id"),
                        new HiddenReportColumn("batchManagement"),
                        new ReportColumn("code", "编号").setColumnWidth(80),
                        new ReportColumn("name", "名称").setTooltip(true),
                        new ReportColumn("specification", "规格", false, null, Arrays.asList("characteristicConverter")).setTooltip(true),
//                        new ReportColumn("base_unit", "基本单位", false, "unit", Arrays.asList("dictConverter")),
                        new HiddenReportColumn("base_unit"),
                        // String name, String label, Boolean sortable, String context, List<String> valueConverterNameList, Integer columnWidth, AlignEnum align, Boolean hidden, Boolean tooltip, TypeEnum type
                        new HiddenReportColumn("base_unit_name", "unit", Arrays.asList("dictConverter"))
//                        new ReportColumn("material_type", "类型", false, "material_type", Arrays.asList("dictConverter")),
//                        new ReportColumn("categoryText", "分类", false, "core_material_category", Arrays.asList("dictConverter"))
                ))
                .pageable(false)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }

    @Test
    public void testSearchReportForSourceList() {
        reportService.saveOrUpdate(Report.builder()
                .id(718618818011729920L)
                .code("mm_material_source_search")
                .tplName("tpl/query_list")
                .name("物料供应商查询")
                .querySql("SELECT cast(mm_material.id as char(20)) id, mm_material.code, mm_material.name, specification, material_type, mm_material.category_id, mm_material.category_id as categoryText, base_unit, base_unit as base_unit_name, standard_price unitPrice, batch_management batchManagement FROM mm_material WHERE mm_material.code = :code AND (mm_material.name like :keywords or specification like :keywords or mm_material.code like :keywords) AND material_type = :materialType AND category_id = :categoryId AND mm_material.is_deleted = 0 AND id = :id AND id IN (:ids) AND exists (select 1 from pur_source_list where partner_id = :partnerId AND (mm_material.id = material_id OR mm_material.category_id = material_category_id))")
                .queryFieldList(Arrays.asList(
                        new QueryField("keywords", "关键字", QueryField.Type.TEXT),
                        new QueryField("categoryId", "分类", QueryField.Type.GROUP_SELECT, "material_category_select_sql")
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new HiddenReportColumn("unitPrice"),
                        new HiddenReportColumn("category_id"),
                        new HiddenReportColumn("batchManagement"),
                        new ReportColumn("code", "编号").setColumnWidth(80),
                        new ReportColumn("name", "名称").setTooltip(true),
                        new ReportColumn("specification", "规格", false, null, Arrays.asList("characteristicConverter")).setTooltip(true),
                        new HiddenReportColumn("base_unit"),
                        new HiddenReportColumn("base_unit_name", "unit", Arrays.asList("dictConverter"))
                ))
                .pageable(false)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }

    @Test
    public void testSearchHasTemplate() {
        reportService.saveOrUpdate(Report.builder()
                .id(719185652091981824L)
                .code("mm_material_template_search")
                .tplName("tpl/query_list")
                .name("bom模版物料查询")
                .querySql("select cast(mm_material.id as char(20)) materialId, mm_material.code code, mm_material.name, mm_material.specification, batch_management batchManagement, base_unit, base_unit as base_unit_name,combine.item_id, combine.partner_id, combine.batch_code, combine.color, combine.remark, combine.create_time  from (SELECT * FROM\n" +
                        "(select null item_id, null partner_id, id material_id, null batch_code, null color, remark, null create_time from mm_material where bom_template_id is NOT NULL) t3\n" +
                        "UNION\n" +
                        "SELECT * FROM\n" +
                        "(select produce_order_item.id item_id, produce_order.partner_id, material_id, batch_code, color, produce_order_item.remark, produce_order_item.create_time  from produce_order_item inner join produce_order on produce_order_item.produce_order_id = produce_order.id order by create_time desc) t4) combine join mm_material on combine.material_id = mm_material.id where material_id = :materialId and partner_id = :partnerId")
                .queryFieldList(Arrays.asList(
//                        new QueryField("code", "编号", QueryField.Type.TEXT),
//                        new QueryField("keywords", "关键字", QueryField.Type.TEXT),
//                        new QueryField("materialType", "类型", QueryField.Type.SELECT, "material_type"),
                        new QueryField("materialId", "产成品物料", QueryField.Type.SELECT, "bom_material"),
                        new QueryField("partnerId", "客户", QueryField.Type.SELECT, "core_partner_customer")
//                        new QueryField("categoryId", "分类", QueryField.Type.GROUP_SELECT, "material_category_select_sql")
//                        new QueryField("categoryId", "分类", QueryField.Type.MULTIPLE_SELECT, "core_material_category")
//                        new QueryField("categoryId", "分类", QueryField.Type.SELECT, "core_material_category")

//                        new QueryField("categoryId", "分类", QueryField.Type.SELECT, "category_path")

                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("materialId"),
                        new HiddenReportColumn("item_id"),
                        new HiddenReportColumn("batchManagement"),
                        new HiddenReportColumn("remark"),
                        new ReportColumn("code", "物料").setColumnWidth(80),
                        new ReportColumn("name", "名称").setTooltip(true),
                        new ReportColumn("specification", "规格", false, null, Arrays.asList("characteristicConverter")).setTooltip(true),
                        new ReportColumn("color", "颜色"),
//                        new ReportColumn("base_unit_name", "基本单位", false, "unit", Arrays.asList("dictConverter")),
                        new HiddenReportColumn("base_unit"),
                        // String name, String label, Boolean sortable, String context, List<String> valueConverterNameList, Integer columnWidth, AlignEnum align, Boolean hidden, Boolean tooltip, TypeEnum type
                        new HiddenReportColumn("base_unit_name", "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("partner_id", "客户", false, "core_partner_customer", Arrays.asList("dictConverter")),
//                        new ReportColumn("remark", "备注"),
                        new ReportColumn("create_time", "生产时间", false,null, Arrays.asList("localDateTimeConverter")).setColumnWidth(160)
//                        new ReportColumn("material_type", "类型", false, "material_type", Arrays.asList("dictConverter")),
//                        new ReportColumn("categoryText", "分类", false, "core_material_category", Arrays.asList("dictConverter"))
                ))
                .pageable(true)
                .build());
    }


}

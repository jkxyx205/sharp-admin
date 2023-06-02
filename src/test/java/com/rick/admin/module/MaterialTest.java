package com.rick.admin.module;

import com.google.common.collect.Lists;
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
                        .pv("label-col", 4)
                        .pv("pane-list", Arrays.asList("一般数据", "量纲", "特征值", "附件"))
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
        CustomizeRegex codeRegex = new CustomizeRegex("^[0-9a-zA-Z_/%-]{1,}$", "CODE只能包含数字、字母、下划线、中划线");
        List<Validator> codeRegexValidatorList = Lists.newArrayList(textRequiredValidatorList);

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
                .datasource("core_material_category")
                .validatorList(Arrays.asList(new Required(true)))
                .additionalInfo(Params.builder(1).pv("pane-index", "1").build())
                .build();

        CpnConfigurer baseUnitCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("baseUnit")
                .label("基本单位")
                .datasource("unit")
                .validatorList(Arrays.asList(new Required(true)))
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
                .name("characteristic")
                .label("特征值")
                .additionalInfo(Params.builder()
                        .pv("columns", Arrays.asList(characteristicKeyCpn, characteristicValueCpn))
                        .pv("pane-index", "3")
                        .build())
                .build();

        CpnConfigurer attachmentCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.FILE)
                .name("attachment")
                .label("附件")
                .additionalInfo(Params.builder(1).pv("pane-index", "4").build())
                .build();

        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(codeCpn, materialTypeCpn, nameCpn, categoryCpn, baseUnitCpn, batchManagementCpn, serialManagementCpn,
                remarkCpn, grossWeightCpn, netWeightCpn, weightUnitCpn, volumeCpn, volumeUnitCpn, sizeCpn, characteristicCpn, attachmentCpn);
        return cpnConfigurerList;
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(695981455636959232L)
                .code("mm_material")
                .tplName("tpl/list")
                .name("物料")
                .extraData("695978675677433856")
                .querySql("SELECT mm_material.id, mm_material.code, mm_material.name, characteristic, case when attachment is null or length(attachment) <= 2 then '无' else '有' end  attachment, material_type, mm_material.category_id, base_unit, sys_user.name create_name,DATE_FORMAT(mm_material.create_time, '%Y-%m-%d %H:%i:%s') create_time FROM mm_material left join sys_user on sys_user.id = mm_material.create_by WHERE mm_material.code = :code AND mm_material.name like :name AND material_type = :materialType AND category_id = :categoryId AND mm_material.is_deleted = 0")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "编号", QueryField.Type.TEXT),
                        new QueryField("materialType", "类型", QueryField.Type.SELECT, "material_type"),
                        new QueryField("categoryId", "分类", QueryField.Type.SELECT, "core_material_category"),
                        new QueryField("name", "名称", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称"),
                        new ReportColumn("characteristic", "特征值", false, null, Arrays.asList("characteristicConverter")),
                        new ReportColumn("material_type", "类型", false, "material_type", Arrays.asList("dictConverter")),
                        new ReportColumn("category_id", "分类", false, "core_material_category", Arrays.asList("dictConverter")),
                        new ReportColumn("base_unit", "基本单位", false, "unit", Arrays.asList("dictConverter")),
                        new ReportColumn("attachment", "附件"),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(180).setAlign(AlignEnum.CENTER)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

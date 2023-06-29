package com.rick.admin.core;

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
 * @date 2023/5/28 15:07
 */
@SpringBootTest
public class PlantTest {

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
                .id(695620585685782528L)
                .code("core_plant")
                .tableName("core_plant")
                .tplName("tpl/form")
                .name("库房")
                .repositoryName("plantDAO")
                .storageStrategy(Form.StorageStrategyEnum.CREATE_TABLE)
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
        textRequiredValidatorList.add(new Length(16));

        // code
        CustomizeRegex codeRegex = new CustomizeRegex("^[0-9a-zA-Z_\\/%\\-]{1,}$", "CODE只能包含数字、字母、下划线、中划线");
        List<Validator> codeRegexValidatorList = Lists.newArrayList(textRequiredValidatorList);

        // long text
        List<Validator> longTextValidatorList = Lists.newArrayListWithExpectedSize(1);
        longTextValidatorList.add(new Length(128));

        codeRegexValidatorList.add(codeRegex);

        CpnConfigurer codeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("code")
                .label("编码")
                .placeholder("请输入编码")
                .validatorList(codeRegexValidatorList)
                .build();

        CpnConfigurer nameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("名称")
                .placeholder("请输入名称")
                .validatorList(textRequiredValidatorList)
                .build();

        CpnConfigurer detailAddressCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("detailAddress")
                .label("详细地址")
                .placeholder("请输入详细地址")
                .validatorList(longTextValidatorList)
                .build();

        CpnConfigurer contactPersonCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("contactPerson")
                .label("联系人")
                .placeholder("请输入联系人")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer contactNumberCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("contactNumber")
                .label("联系电话")
                .placeholder("请输入联系电话")
                .validatorList(textValidatorList)
                .build();


        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(codeCpn, nameCpn, detailAddressCpn, contactPersonCpn, contactNumberCpn);
        return cpnConfigurerList;
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(695623902415556608L)
                .code("core_plant")
                .tplName("tpl/list")
                .name("库房")
                .additionalInfo(Params.builder(1).pv("formId", "695620585685782528").build())
                .querySql("SELECT core_plant.id, core_plant.code, core_plant.name, detail_address, contact_person, contact_number, sys_user.name create_name,DATE_FORMAT(core_plant.create_time, '%Y-%m-%d %H:%i:%s') create_time FROM core_plant left join sys_user on sys_user.id = core_plant.create_by WHERE core_plant.code = :code AND core_plant.name like :name AND core_plant.is_deleted = 0")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "编号", QueryField.Type.TEXT),
                        new QueryField("name", "名称", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称"),
                        new ReportColumn("detail_address", "详细地址"),
                        new ReportColumn("contact_person", "联系人"),
                        new ReportColumn("contact_number", "联系电话"),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(180).setAlign(AlignEnum.CENTER)
                ))
                .pageable(false)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

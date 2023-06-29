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
public class PartnerTest {

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
                .id(695708313425285120L)
                .code("core_partner")
                .tableName("core_partner")
                .tplName("modules/partner")
                .name("合作伙伴")
                .repositoryName("partnerDAO")
                .formAdviceName("partnerFormService")
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
        textRequiredValidatorList.add(new Length(32));

        // code
        CustomizeRegex codeRegex = new CustomizeRegex("^[0-9a-zA-Z_\\/%\\-]{1,}$", "CODE只能包含数字、字母、下划线、中划线");
        List<Validator> codeRegexValidatorList = Lists.newArrayList(textRequiredValidatorList);
        codeRegexValidatorList.add(codeRegex);

        // long text
        List<Validator> longTextValidatorList = Lists.newArrayListWithExpectedSize(1);
        longTextValidatorList.add(new Length(1000));

        CpnConfigurer typeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.RADIO)
                .datasource("partner_type")
                .name("partnerType")
                .label("类型")
                .defaultValue("CUSTOMER")
                .validatorList(Arrays.asList(new Required(true)))
                .build();

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
                .label("公司名称")
                .placeholder("请输入公司名称")
                .validatorList(textRequiredValidatorList)
                .build();

        CpnConfigurer remarkCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXTAREA)
                .name("remark")
                .label("备注")
                .placeholder("请输入备注")
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

        CpnConfigurer contactMailCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.EMAIL)
                .name("contactMail")
                .label("联系邮箱")
                .placeholder("请输入联系邮箱")
                .build();

        CpnConfigurer bankNameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("bankName")
                .label("开户银行")
                .placeholder("请输入开户银行")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer bankAccountCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("bankAccount")
                .label("银行账户")
                .placeholder("请输入银行账户")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer taxCodeCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("taxCode")
                .label("纳税人识别号")
                .placeholder("请输入纳税人识别号")
                .validatorList(Arrays.asList(new Length(18)))
                .build();

        CpnConfigurer addressCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("address")
                .label("公司地址")
                .placeholder("请输入公司地址")
                .validatorList(Arrays.asList(new Length(128)))
                .build();


        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(typeCpn, codeCpn, nameCpn, remarkCpn, contactPersonCpn, contactNumberCpn, contactMailCpn,
                bankNameCpn, bankAccountCpn, taxCodeCpn, addressCpn);
        return cpnConfigurerList;
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(695710879554998272L)
                .code("core_partner")
                .tplName("tpl/list")
                .name("合作伙伴")
                .additionalInfo(Params.builder(1).pv("formId", "695708313425285120").build())
                .querySql("SELECT core_partner.id, core_partner.partner_type, core_partner.code, core_partner.name, core_partner.remark, core_partner.contact_person, core_partner.contact_number, core_partner.contact_mail, core_partner.bank_name, core_partner.bank_account, core_partner.tax_code, core_partner.address, sys_user.name create_name,DATE_FORMAT(core_partner.create_time, '%Y-%m-%d %H:%i:%s') create_time FROM core_partner left join sys_user on sys_user.id = core_partner.create_by WHERE core_partner.partner_type = :partnerType AND core_partner.code = :code AND core_partner.name like :name AND core_partner.is_deleted = 0")
                .queryFieldList(Arrays.asList(
                        new QueryField("partnerType", "类型", QueryField.Type.SELECT, "partner_type"),
                        new QueryField("code", "编号", QueryField.Type.TEXT),
                        new QueryField("name", "名称", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "编号"),
                        new ReportColumn("name", "名称"),
                        new ReportColumn("partner_type", "类型", false, "partner_type", Arrays.asList("dictConverter")),
                        new ReportColumn("contact_person", "联系人"),
                        new ReportColumn("contact_number", "联系电话"),
                        new ReportColumn("contact_mail", "联系邮箱"),
//                        new ReportColumn("address", "公司地址"),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(180).setAlign(AlignEnum.CENTER)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }
}

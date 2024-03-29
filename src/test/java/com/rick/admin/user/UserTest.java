package com.rick.admin.user;

import com.google.common.collect.Lists;
import com.rick.admin.sys.permission.dao.PermissionDAO;
import com.rick.admin.sys.permission.entity.Permission;
import com.rick.admin.sys.role.dao.RoleDAO;
import com.rick.admin.sys.role.entity.Role;
import com.rick.admin.sys.user.dao.UserDAO;
import com.rick.admin.sys.user.entity.User;
import com.rick.admin.sys.user.service.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:07
 */
@SpringBootTest
public class UserTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private PermissionDAO permissionDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReportService reportService;

    @Autowired
    private FormCpnService formCpnService;

    @Autowired
    private CpnConfigurerService cpnConfigurerService;

    @Autowired
    private FormService formService;

    @Autowired
    private UserService userService;

    @Test
    public void testGetIdNameMapping() {
        System.out.println(userService.getIdNameMapping());
    }

    @Test
    public void insertOrUpdateUser() {
        userDAO.insertOrUpdate(User.builder()
                .code("admin")
                .name("Rick.Xu")
                .password("$2a$10$0b1kYsx5IFp0uJw1dA0bquirueMbeIs2O4iqm1ALJLZ.RQByR2wcq")
                .createBy(0L)
                .updateBy(0L)
                .available(true )
                .build());
    }

    @Test
    public void insertOrUpdatePermission() {
//        permissionDAO.insertOrUpdate(Permission.builder()
//                .code("mm_material")
//                .name("物料")
//                .pid(695624494395428864L)
//                .permissionOrder(3)
//                .build());

        permissionDAO.insertOrUpdate(Permission.builder()
                .code("mm_material_edit")
                .name("编辑")
                .pid(695981915009716224L)
                .permissionOrder(1)
                .build());

        permissionDAO.insertOrUpdate(Permission.builder()
                .code("mm_material_delete")
                .name("删除")
                .pid(695981915009716224L)
                .permissionOrder(2)
                .build());
    }

    @Test
    public void insertOrUpdateRole() {
        roleDAO.insertOrUpdate(Role.builder()
//                .id(694230445579046912L)
                .code("admin")
                .name("管理员")
                .permissionList(Arrays.asList(Permission.builder().id(694587393189089280L).build()))
                .userList(Arrays.asList(User.builder().id(694587333101490176L).build()))
                .build());
    }

    @Test
    public void testFindUserByCode() {
        Optional<User> optionalUser = userDAO.selectByCode("admin");
        User user = optionalUser.get();
    }

    @Test
    public void testPassword() {
        System.out.println(passwordEncoder.encode("111111"));
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(694714180413960192L)
                .code("sys_user")
                .tplName("tpl/list")
                .name("用户管理")
                .additionalInfo(Params.builder(1).pv("formId", "694980924206493696").build())
//                .querySql("SELECT id, code, name, IF(is_available, '是', '否') is_available, DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') create_time FROM sys_user WHERE code LIKE :code AND name LIKE :name AND is_available = :is_available AND create_time >= :create_time0 AND create_time <= :create_time1 AND id <> 1")
                // language=SQL
                .querySql(" SELECT sys_user.id, sys_user.code, sys_user.name, IF(sys_user.is_available, '是', '否') is_available, t.name role_name, u.name create_name, DATE_FORMAT(sys_user.create_time, '%Y-%m-%d %H:%i:%s') create_time FROM sys_user\n" +
                        " LEFT JOIN sys_user u on u.id = sys_user.create_by\n" +
                        " LEFT JOIN ( SELECT sys_user.id, GROUP_CONCAT(r.name) name FROM sys_user\n" +
                        " LEFT JOIN sys_user_role ur on sys_user.id = ur.user_id AND ur.is_deleted = 0\n" +
                        " LEFT JOIN sys_role r on r.id = ur.role_id AND r.is_deleted = 0\n" +
                        " group by sys_user.id order by sys_user.id asc) t on t.id = sys_user.id\n" +
                        "WHERE sys_user.code LIKE :code AND sys_user.name LIKE :name AND sys_user.is_available = :is_available AND sys_user.create_time >= :create_time0 AND sys_user.create_time <= :create_time1 AND sys_user.is_deleted = 0")
                .queryFieldList(Arrays.asList(
                        new QueryField("code", "用户名"),
                        new QueryField("name", "姓名"),
                        new QueryField("is_available", "是否可用", QueryField.Type.SELECT, "bol"),
                        new QueryField("create_time", "创建时间", QueryField.Type.DATE_RANGE)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
                        new ReportColumn("code", "用户名", true),
                        new ReportColumn("name", "姓名", true),
                        new ReportColumn("role_name", "角色"),
                        new ReportColumn("is_available", "是否可用").setColumnWidth(80).setAlign(AlignEnum.CENTER),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(180).setAlign(AlignEnum.CENTER)
                ))
                .pageable(true)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }

    @Test
    public void testForm() {
        // 设计控件
        List<CpnConfigurer> cpnConfigurerList = createCpnConfigurerList();
        cpnConfigurerService.saveOrUpdate(cpnConfigurerList);
        // 创建表
        Form form = formService.saveOrUpdate(Form.builder()
                .id(694980924206493696L)
                .code("sys_user")
                .tableName("sys_user")
                .tplName("tpl/form")
                .name("人员信息表")
                .formAdviceName("userFormAdvice")
                .repositoryName("userDAO")
                .storageStrategy(Form.StorageStrategyEnum.CREATE_TABLE)
                .build());
        // 关联关系
        formCpnService.saveOrUpdateByConfigIds(form.getId(), cpnConfigurerList.stream().map(CpnConfigurer::getId).collect(Collectors.toList()));
    }

    private List<CpnConfigurer> createCpnConfigurerList() {
        // 文本校验器
        List<Validator> textValidatorList = Lists.newArrayListWithExpectedSize(2);
        textValidatorList.add(new Required(true));
        textValidatorList.add(new Length(16));

        CustomizeRegex codeRegex = new CustomizeRegex("^[0-9a-zA-Z_\\/%\\-]{1,}$", "CODE只能包含数字、字母、下划线、中划线");

        List<Validator> codeRegexValidatorList = Lists.newArrayList(textValidatorList);
        codeRegexValidatorList.add(codeRegex);

        CpnConfigurer usernameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("code")
                .label("用户名")
                .placeholder("请输入用户名")
                .validatorList(codeRegexValidatorList)
                .build();

        CpnConfigurer nameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("姓名")
                .placeholder("请输入姓名")
                .validatorList(textValidatorList)
                .build();

        CpnConfigurer availableCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SWITCH)
                .name("available")
                .label("可用")
                .defaultValue("1")
                .build();

        CpnConfigurer roleCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.CHECKBOX)
                .datasource("sys_role")
//                .options(Arrays.asList(new CpnConfigurer.CpnOption("694587732420202496", "管理员")))
                .name("roleIds")
                .label("角色")
                .build();

        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(usernameCpn, nameCpn, availableCpn, roleCpn);
        return cpnConfigurerList;
    }
}

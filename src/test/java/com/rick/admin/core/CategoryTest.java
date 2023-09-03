package com.rick.admin.core;

import com.google.common.collect.Lists;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.plugin.ztree.model.TreeNodeService;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.CpnConfigurerService;
import com.rick.formflow.form.service.FormCpnService;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.valid.Length;
import com.rick.formflow.form.valid.Required;
import com.rick.formflow.form.valid.core.Validator;
import com.rick.report.core.entity.Report;
import com.rick.report.core.model.*;
import com.rick.report.core.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:07
 */
@SpringBootTest
public class CategoryTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private FormCpnService formCpnService;

    @Autowired
    private CpnConfigurerService cpnConfigurerService;

    @Autowired
    private FormService formService;

    @Resource
    private TreeNodeService treeNodeService;

    @Test
    public void testForm() {
        // 设计控件
        List<CpnConfigurer> cpnConfigurerList = createCpnConfigurerList();
        cpnConfigurerService.saveOrUpdate(cpnConfigurerList);
        // 创建表
        Form form = formService.saveOrUpdate(Form.builder()
                .id(695658661183229952L)
                .code("core_material_category")
                .tableName("core_material_category")
                .tplName("tpl/form")
                .formAdviceName("categoryFormService")
                .name("物料分类")
                .repositoryName("categoryDAO")
                .storageStrategy(Form.StorageStrategyEnum.CREATE_TABLE)
                .build());
        // 关联关系
        formCpnService.saveOrUpdateByConfigIds(form.getId(), cpnConfigurerList.stream().map(CpnConfigurer::getId).collect(Collectors.toList()));
    }

    private List<CpnConfigurer> createCpnConfigurerList() {
        // 文本校验器
        List<Validator> textRequiredValidatorList = Lists.newArrayListWithExpectedSize(2);
        textRequiredValidatorList.add(new Required(true));
        textRequiredValidatorList.add(new Length(16));

        CpnConfigurer parentIdCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.SELECT)
                .name("parentId")
                .label("上级分类")
//                .datasource("core_material_category")
                .datasource("TREE_CATEGORY")
                .build();

        CpnConfigurer nameCpn = CpnConfigurer.builder()
                .cpnType(CpnTypeEnum.TEXT)
                .name("name")
                .label("名称")
                .placeholder("请输入分类名称")
                .validatorList(textRequiredValidatorList)
                .build();



        List<CpnConfigurer> cpnConfigurerList = Lists.newArrayList(parentIdCpn, nameCpn);
        return cpnConfigurerList;
    }

    @Test
    public void testReport() {
        reportService.saveOrUpdate(Report.builder()
                .id(695661483089989632L)
                .code("core_material_category")
                .tplName("tpl/list")
                .name("分类")
                .additionalInfo(Params.builder(1).pv("formId", "695658661183229952").build())
                .querySql("SELECT core_material_category.id, core_material_category.parent_id, core_material_category.name, p.name parent_name, sys_user.name create_name,DATE_FORMAT(core_material_category.create_time, '%Y-%m-%d %H:%i:%s') create_time FROM core_material_category left join sys_user on sys_user.id = core_material_category.create_by left join core_material_category p on p.id = core_material_category.parent_id WHERE core_material_category.parent_id = :parentId AND core_material_category.name like :name AND core_material_category.is_deleted = 0")
                .queryFieldList(Arrays.asList(
                        new QueryField("parentId", "上级分类", QueryField.Type.SELECT, "TREE_CATEGORY"),
                        new QueryField("name", "名称", QueryField.Type.TEXT)
                ))
                .reportColumnList(Arrays.asList(
                        new HiddenReportColumn("id"),
//                        new HiddenReportColumn("parent_id"),
                        new ReportColumn("name", "名称"),
//                        new ReportColumn("parent_name", "上级分类", false, Arrays.asList("categoryValueConverter")),
                        new ReportColumn("parent_id", "上级分类", false, Arrays.asList("categoryValueConverter")),
                        new ReportColumn("create_name", "创建人").setColumnWidth(100),
                        new ReportColumn("create_time", "创建时间").setColumnWidth(180).setAlign(AlignEnum.CENTER)
                ))
                .pageable(false)
                .sidx("id")
                .sord(SordEnum.ASC)
                .build());
    }

    @Test
    public void testCategoryService() {
        List<TreeNode> selectTreeNode = treeNodeService.getSelectTreeNode("select id, name, parent_id \"pId\" from `core_material_category` order by order_index asc", null);
        System.out.println(selectTreeNode);
    }
}

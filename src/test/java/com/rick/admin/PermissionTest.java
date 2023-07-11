package com.rick.admin;

import com.google.common.collect.Sets;
import com.rick.admin.sys.permission.dao.PermissionDAO;
import com.rick.admin.sys.permission.entity.Permission;
import com.rick.admin.sys.role.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:41
 */
@SpringBootTest
public class PermissionTest {

    @Autowired
    private PermissionDAO permissionDAO;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testAddPermission0() {
//        permissionDAO.insertOrUpdate(Permission.builder()
//                .code("produce_order_list")
//                .name("生产单管理")
//                .pid(708744048906604544L)
//                .permissionOrder(2)
//                .build());


        permissionDAO.insertOrUpdate(Permission.builder()
                .code("produce_order_stock")
                .name("生产库存")
                .pid(708744048906604544L)
                .permissionOrder(3)
                .build());


    }


    @Test
    public void testAddPermission1() {
//        final String CODE = "mm_material";
//        final String NAME = "物料";
//        final Long PARENT_ID = 695624494395428864L;
//
//        final String CODE = "core_plant";
//        final String NAME = "库房";
//        final Long PARENT_ID = 695624494395428864L;

//        final String CODE = "core_material_category";
//        final String NAME = "物料分类";
//        final Long PARENT_ID = 695624494395428864L;

//        final String CODE = "core_partner";
//        final String NAME = "合作伙伴";
//        final Long PARENT_ID = 695624494395428864L;

//        final String CODE = "sys_user";
//        final String NAME = "用户管理";
//        final Long PARENT_ID = 695624494395428864L;

//        final String CODE = "sys_dict";
//        final String NAME = "字典管理";
//        final Long PARENT_ID = 695624494395428864L;

//        final String CODE = "pur_purchase_order";
//        final String NAME = "采购订单";
//        final Long PARENT_ID = 702223996619358208L;
//        final int PERMISSION_ORDER = 3;

        final String CODE = "produce_order";
        final String NAME = "生产单";
        final Long PARENT_ID = 708744048906604544L;
        final int PERMISSION_ORDER = 2;

        jdbcTemplate.execute("delete from sys_role_permission where permission_id IN (select id from sys_permission where code like'"+CODE+"%')");

        jdbcTemplate.execute("delete from sys_permission where code like'"+CODE+"%'");

        Permission root = Permission.builder()
                .code(CODE)
                .name(NAME)
                .pid(PARENT_ID)
                .permissionOrder(PERMISSION_ORDER)
                .build();

        permissionDAO.insertOrUpdate(root);

        Permission read = Permission.builder()
                .code(CODE + "_read")
                .name("查看")
                .pid(root.getId())
                .permissionOrder(0)
                .build();

        Permission add = Permission.builder()
                .code(CODE + "_add")
                .name("新增")
                .pid(root.getId())
                .permissionOrder(1)
                .build();

        Permission edit = Permission.builder()
                .code(CODE + "_edit")
                .name("编辑")
                .pid(root.getId())
                .permissionOrder(2)
                .build();

        Permission delete = Permission.builder()
                .code(CODE + "_delete")
                .name("删除")
                .pid(root.getId())
                .permissionOrder(3)
                .build();

        permissionDAO.insertOrUpdate(read);
        permissionDAO.insertOrUpdate(add);
        permissionDAO.insertOrUpdate(edit);
        permissionDAO.insertOrUpdate(delete);

        // 管理员分配权限
        List<Long> permissionIds = permissionDAO.selectByParams(null, "id", Long.class);
        roleService.addPermission(694587732420202496L, Sets.newHashSet(permissionIds));
    }

}

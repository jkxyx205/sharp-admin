package com.rick.admin;

import com.google.common.collect.Sets;
import com.rick.admin.sys.permission.dao.PermissionDAO;
import com.rick.admin.sys.permission.entity.Permission;
import com.rick.admin.sys.role.service.RoleService;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
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

    @Autowired
    private DictService dictService;

    @Test
    public void testAddPermission0() {
//        permissionDAO.insertOrUpdate(Permission.builder()
//                .code("produce_order_list")
//                .name("生产单管理")
//                .pid(708744048906604544L)
//                .permissionOrder(2)
//                .build());


        permissionDAO.insertOrUpdate(Permission.builder()
                .code("stock_age")
                .name("库龄分析")
                .pid(708744048906604544L)
                .permissionOrder(4)
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

        final String CODE = "bom_list";
        final String NAME = "物料清单";
        final Long PARENT_ID = 708744048906604544L;
        final int PERMISSION_ORDER = 0;

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

    @Test
    public void addPlantPermission() {
        final String CODE = "movement_plant";
        final String NAME = "库房";
        final Long PARENT_ID = 697500384922439680L;
        final int PERMISSION_ORDER = 0;

        jdbcTemplate.execute("delete from sys_role_permission where permission_id IN (select id from sys_permission where code like'"+CODE+"%')");
        jdbcTemplate.execute("delete from sys_permission where code like'"+CODE+"%'");

        Permission root = Permission.builder()
                .code(CODE)
                .name(NAME)
                .pid(PARENT_ID)
                .permissionOrder(PERMISSION_ORDER)
                .build();

        permissionDAO.insertOrUpdate(root);

        List<Dict> plantList = dictService.getDictByType("core_plant");
        int i = 0;
        for (Dict dict : plantList) {
            Permission permission = Permission.builder()
                    .code(dict.getName())
                    .name(dict.getLabel())
                    .pid(root.getId())
                    .permissionOrder(i++)
                    .build();
            System.out.println(dict);

            permissionDAO.insertOrUpdate(permission);
        }

        // 管理员分配权限
        List<Long> permissionIds = permissionDAO.selectByParams(null, "id", Long.class);
        roleService.addPermission(694587732420202496L, Sets.newHashSet(permissionIds));
    }

    @Test
    public void addMovementTypePermission() {
        final String CODE = "movement_type";
        final String NAME = "移动类型";
        final Long PARENT_ID = 697500384922439680L;
        final int PERMISSION_ORDER = 0;

        jdbcTemplate.execute("delete from sys_role_permission where permission_id IN (select id from sys_permission where code like'"+CODE+"%')");
        jdbcTemplate.execute("delete from sys_permission where code like'"+CODE+"%'");

        Permission root = Permission.builder()
                .code(CODE)
                .name(NAME)
                .pid(PARENT_ID)
                .permissionOrder(PERMISSION_ORDER)
                .build();

        permissionDAO.insertOrUpdate(root);


        Permission permission1 = Permission.builder()
                .code("INBOUND")
                .name("收货")
                .pid(root.getId())
                .permissionOrder(0)
                .build();
        Permission permission2 = Permission.builder()
                .code("OUTBOUND")
                .name("出货")
                .pid(root.getId())
                .permissionOrder(1)
                .build();
        Permission permission3 = Permission.builder()
                .code("RETURN")
                .name("退货")
                .pid(root.getId())
                .permissionOrder(2)
                .build();
        Permission permission4 = Permission.builder()
                .code("MOVING_TO_PRODUCE")
                .name("领料")
                .pid(root.getId())
                .permissionOrder(3)
                .build();
        Permission permission5 = Permission.builder()
                .code("RETURN_FROM_PRODUCE")
                .name("退料")
                .pid(root.getId())
                .permissionOrder(4)
                .build();

        Permission permission6 = Permission.builder()
                .code("DELETE")
                .name("删除")
                .pid(root.getId())
                .permissionOrder(5)
                .build();

        permissionDAO.insertOrUpdate(permission1);
        permissionDAO.insertOrUpdate(permission2);
        permissionDAO.insertOrUpdate(permission3);
        permissionDAO.insertOrUpdate(permission4);
        permissionDAO.insertOrUpdate(permission5);
        permissionDAO.insertOrUpdate(permission6);

        // 管理员分配权限
        List<Long> permissionIds = permissionDAO.selectByParams(null, "id", Long.class);
        roleService.addPermission(694587732420202496L, Sets.newHashSet(permissionIds));
    }

}

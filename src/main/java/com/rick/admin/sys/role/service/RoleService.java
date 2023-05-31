package com.rick.admin.sys.role.service;

import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.sys.permission.PermissionService;
import com.rick.admin.sys.role.dao.RoleDAO;
import com.rick.admin.sys.role.entity.Role;
import com.rick.admin.sys.role.model.RoleInfoDTO;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/31 21:41
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class RoleService {

    RoleDAO roleDAO;

    PermissionService permissionService;

    public void removeRoleByUserId(Long roleId, String userId) {
        SQLUtils.delete("SYS_USER_ROLE", new Object[] {roleId, userId}, "ROLE_ID = ? and USER_ID = ?");
    }

    public List<Role> editRoles(List<Role> roleList) {
        if (CollectionUtils.isEmpty(roleList)) {
            roleDAO.deleteAll();
            SQLUtils.execute("delete from sys_role_permission");
            SQLUtils.execute("delete from sys_user_role");
        } else {
            Set<Long> roleIds = roleList.stream().map(Role::getId).collect(Collectors.toSet());
            SQLUtils.deleteNotIn(roleDAO.getTableName(), "id", roleIds);
            SQLUtils.deleteNotIn("sys_role_permission", "role_id", roleIds);
            SQLUtils.deleteNotIn("sys_user_role", "role_id", roleIds);

            for (Role role : roleList) {
                if (Objects.isNull(role.getId())) {
                    role.setCode(RandomStringUtils.randomAlphanumeric(5));
                    roleDAO.insert(role);
                } else {
                    roleDAO.update("name", Params.builder(1).pv("id", role.getId()).pv("name", role.getName()).build(), "id = :id");
                }
            }
        }

        return roleList;
    }

    public RoleInfoDTO getSettingsInfoByRoleId(Long roleId) {
        Role role = roleDAO.selectById(roleId).get();
        List<TreeNode> treeNodeList = permissionService.findTreeNodeByRoleIds(Arrays.asList(roleId));

        RoleInfoDTO roleInfo = new RoleInfoDTO(role.getUserList(), treeNodeList);
        return roleInfo;
    }

    public void addPermission(Long roleId, Set<Long> permissionIds) {
        SQLUtils.updateRefTable("sys_role_permission", "role_id", "permission_id", roleId, permissionIds);
    }

    /**
     * 为角色添加用户
     * @param roleId
     * @param userIds
     */
    public void addUser(long roleId, Set<String> userIds) {
        SQLUtils.updateRefTable("sys_user_role", "role_id", "user_id", roleId, userIds);
    }
}
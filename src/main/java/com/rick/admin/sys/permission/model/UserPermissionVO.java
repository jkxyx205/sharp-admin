package com.rick.admin.sys.permission.model;

import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.sys.role.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/5/31 21:27
 */
@Data
@Builder
public class UserPermissionVO {

    private List<Role> roleList;

    private List<TreeNode> permissionList;

}

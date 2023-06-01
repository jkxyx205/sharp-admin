package com.rick.admin.sys.permission;

import com.google.common.collect.Maps;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.db.service.SharpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/5/31 21:31
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class PermissionService {

    SharpService sharpService;

    public List<TreeNode> findTreeNodeByRoleIds(Collection<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("roleIds", roleIds);

        return sharpService.query("SELECT p.id as \"id\", p.name as \"name\", pid as \"pId\", 1 as open\n" +
                "   FROM sys_permission P\n" +
                "   WHERE EXISTS(select 1 from sys_ROLE_permission rp, sys_role r WHERE R.id = rp.role_id AND p.id = rp.permission_id AND r.id IN (:roleIds)) and p.is_deleted = 0 order by p.permission_order asc" +
                "   ", params, TreeNode.class);
    }

}
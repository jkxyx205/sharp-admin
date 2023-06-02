package com.rick.admin.sys.permission.service;

import com.google.common.collect.Maps;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.db.service.SharpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

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

    static final int MAX_EXPEND_SIZE = 30;

    public List<TreeNode> findTreeNodeByRoleIds(Collection<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("roleIds", roleIds);

        List<TreeNode> treeList = sharpService.query("SELECT p.id as \"id\", p.name as \"name\", pid as \"pId\", 1 as open\n" +
                "   FROM sys_permission P\n" +
                "   WHERE EXISTS(select 1 from sys_ROLE_permission rp, sys_role r WHERE R.id = rp.role_id AND p.id = rp.permission_id AND r.id IN (:roleIds)) and p.is_deleted = 0 order by p.permission_order asc" +
                "   ", params, TreeNode.class);

        if (treeList.size() <= MAX_EXPEND_SIZE) {
            return treeList;
        }

        // 子节点收起
        Map<Long, TreeNode> treeNodeMap = treeList.stream().collect(Collectors.toMap(TreeNode::getId, treeNode -> treeNode));
        Set<Long> parentIds = treeList.stream().map(TreeNode::getPId).collect(Collectors.toSet());
        Set<TreeNode> leafNodeSet = treeList.stream().filter(treeNode -> !parentIds.contains(treeNode.getId()) && treeNode.getPId() != 0).collect(Collectors.toSet());

        for (TreeNode treeNode : leafNodeSet) {
            treeNode.setOpen(false);

            TreeNode parentNode = treeNodeMap.get(treeNode.getPId());
            boolean allLeafNode = true;
            for (TreeNode node : treeList) {
                if (Objects.equals(node.getPId(), parentNode.getId())) {
                    if (!leafNodeSet.contains(node)) {
                        allLeafNode = false;
                    }
                }
            }

            if (allLeafNode) {
                parentNode.setOpen(false);
            }
        }

        return treeList;
    }

}
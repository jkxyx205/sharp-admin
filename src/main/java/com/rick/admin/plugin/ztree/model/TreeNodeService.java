package com.rick.admin.plugin.ztree.model;

import com.rick.db.service.SharpService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Rick.Xu
 * @date 2023/9/2 21:14
 */
@Service
@RequiredArgsConstructor
public class TreeNodeService {

    private final SharpService sharpService;

    public List<TreeNode> getSelectTreeNode(String querySql) {
        List<TreeNode> treeNodeList = getStructuredTreeNode(querySql);

        List<TreeNode> containerList = new ArrayList<>();
        recursiveStructuredTreeNode(treeNodeList, treeNode -> {
            treeNode.setName(StringUtils.repeat("&nbsp;&nbsp;&nbsp;&nbsp;", treeNode.getLevel()) + treeNode.getName());
            containerList.add(treeNode);
        });

        return containerList;
    }

    public List<TreeNode> getStructuredTreeNode(String querySql) {
        List<TreeNode> treeNodeList = sharpService.query(querySql, null, TreeNode.class);
        List<TreeNode> ret = recursive(treeNodeList, 0L, 0);
        return ret;
    }

    private List<TreeNode> recursive(List<TreeNode> treeNodeList, Long pid, int level) {
        List<TreeNode> _children = new ArrayList<>();
        List<TreeNode> otherNode = new ArrayList<>();

        treeNodeList.forEach(node -> {
            if (Objects.equals(node.getPId(), pid)) {
                _children.add(node);
                node.setLevel(level);
            } else {
                otherNode.add(node);
            }
        });

        if (_children.size() > 0) {
            _children.forEach(_node -> {
                List<TreeNode> node_child = recursive(otherNode, _node.getId(), level + 1);
                if (node_child.size() > 0) {
                    _node.setSubTreeNodeList(node_child);
                }
            });
        }

        return _children;
    }

    private void recursiveStructuredTreeNode(List<TreeNode> treeNodeList, Consumer<TreeNode> treeNodeConsumer) {
        if (CollectionUtils.isEmpty(treeNodeList)) {
            return;
        }

        for (TreeNode treeNode : treeNodeList) {
            treeNodeConsumer.accept(treeNode);
            recursiveStructuredTreeNode(treeNode.getSubTreeNodeList(), treeNodeConsumer);
        }
    }

}

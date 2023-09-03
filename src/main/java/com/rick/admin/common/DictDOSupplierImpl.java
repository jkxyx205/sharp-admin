package com.rick.admin.common;

import com.google.common.collect.Lists;
import com.rick.admin.module.core.service.CategoryService;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.plugin.ztree.model.TreeNodeService;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictDOSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/7 00:23
 */
@Component
@RequiredArgsConstructor
public class DictDOSupplierImpl implements DictDOSupplier {

    private final CategoryService categoryService;

    public final static String CATEGORY_PATH = "category_path";

    public final static String TREE_CATEGORY = "TREE_CATEGORY";

    private final TreeNodeService treeNodeService;
    
    @Override
    public List<Dict> get() {
        // category
        List<Dict> dictList = Lists.newArrayListWithExpectedSize(categoryService.getIdCategoryPathMap().size());

        for (Map.Entry<Long, String> entry : categoryService.getIdCategoryPathMap().entrySet()) {
            dictList.add(new Dict(CATEGORY_PATH, String.valueOf(entry.getKey()), entry.getValue(), 1));
        }

        // 树形结构
        List<TreeNode> selectTreeNode = treeNodeService.getSelectTreeNode("select id, name, parent_id \"pId\" from `core_material_category` order by order_index asc", null);
        List<Dict> colorDict = selectTreeNode.stream().map(node -> new Dict(TREE_CATEGORY, node.getId().toString(), node.getName(), null)).collect(Collectors.toList());
        dictList.addAll(colorDict);

        return dictList;
    }
}

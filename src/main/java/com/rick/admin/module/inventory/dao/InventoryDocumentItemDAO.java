package com.rick.admin.module.inventory.dao;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.db.plugin.dao.core.ColumnFillType;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/6/7 15:51
 */
@Repository
@RequiredArgsConstructor
public class InventoryDocumentItemDAO extends EntityDAOImpl<InventoryDocument.Item, Long> {

    @Override
    protected Object[] handleAutoFill(Object t, Object[] params, List<String> columnNameList, ColumnFillType fillType) {
        Object[] objects = super.handleAutoFill(t, params, columnNameList, fillType);
        int rootReferenceItemIdIndex = columnNameList.indexOf("root_reference_item_id");
        if (rootReferenceItemIdIndex != -1 && objects[rootReferenceItemIdIndex] == null) {
            objects[rootReferenceItemIdIndex] = objects[columnNameList.indexOf(getIdColumnName())];
        }
        return objects;
    }
}
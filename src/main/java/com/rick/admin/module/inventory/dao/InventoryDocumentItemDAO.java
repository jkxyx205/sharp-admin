package com.rick.admin.module.inventory.dao;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2023/6/7 15:51
 */
@Repository
public class InventoryDocumentItemDAO extends EntityDAOImpl<InventoryDocument.Item, Long> {

}
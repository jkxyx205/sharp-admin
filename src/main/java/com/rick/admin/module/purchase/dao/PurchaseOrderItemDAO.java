package com.rick.admin.module.purchase.dao;

import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2023/6/19 16:23
 */
@Repository
public class PurchaseOrderItemDAO extends EntityDAOImpl<PurchaseOrder.Item, Long> {

}
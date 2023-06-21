package com.rick.admin.module.purchase.dao;

import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.db.service.support.Params;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/6/19 16:23
 */
@Repository
public class PurchaseOrderItemDAO extends EntityDAOImpl<PurchaseOrder.Item, Long> {

    public void setComplete(List<Long> completeIdList) {
        // set complete = true
        if (CollectionUtils.isNotEmpty(completeIdList)) {
            update("is_complete"
                    , Params.builder(2).pv("completeIdList", completeIdList).pv("is_complete", true).build(),
                    "id IN (:completeIdList)");
        }
    }
}
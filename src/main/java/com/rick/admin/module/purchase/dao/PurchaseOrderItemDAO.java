package com.rick.admin.module.purchase.dao;

import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.db.service.support.Params;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/6/19 16:23
 */
@Repository
@RequiredArgsConstructor
public class PurchaseOrderItemDAO extends EntityDAOImpl<PurchaseOrder.Item, Long> {

    public void setCompleted(List<Long> completeIdList) {
        setCompleteStatus(completeIdList, true);
    }

    public void setUnCompleted(List<Long> completeIdList) {
        setCompleteStatus(completeIdList, false);
    }

    public List<PurchaseOrder.Item> list(String purchaseOrderCode) {
        return selectByParams("purchase_order_code="+purchaseOrderCode);
    }

    public List<Boolean> complateStatusList(String purchaseOrderCode) {
        return selectByParams(Params.builder(1).pv("purchaseOrderCode", purchaseOrderCode).build(),
                "is_complete", "purchase_order_code= :purchaseOrderCode", Boolean.class);
    }

    private void setCompleteStatus(List<Long> completeIdList, boolean status) {
        if (CollectionUtils.isNotEmpty(completeIdList)) {
            update("is_complete"
                    , Params.builder(2).pv("completeIdList", completeIdList).pv("is_complete", status).build(),
                    "id IN (:completeIdList)");
        }
    }
}
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

//    final ClassificationDAO materialClassificationDAO;

    public void setComplete(List<Long> completeIdList) {
        // set complete = true
        if (CollectionUtils.isNotEmpty(completeIdList)) {
            update("is_complete"
                    , Params.builder(2).pv("completeIdList", completeIdList).pv("is_complete", true).build(),
                    "id IN (:completeIdList)");
        }
    }

    public List<PurchaseOrder.Item> list(String purchaseOrderCode) {
        return selectByParams("purchase_order_code="+purchaseOrderCode);
    }

//    @Override
//    protected <E> List<E> selectByParams(Map<String, ?> params, String columnNames, String conditionSQL, SqlHandler sqlHandler, Class<E> clazz) {
//        List<E> list = super.selectByParams(params, columnNames, conditionSQL, sqlHandler, clazz);
//        for (E e : list) {
//            PurchaseOrder.Item item = (PurchaseOrder.Item) e;
//            // 手动加载防止两个相同的物料， 特征分组是相同的引用
//            item.setClassificationList(materialClassificationDAO.selectByParams(Classification.builder().materialId(item.getMaterialId()).build()));
//        }
//        return list;
//    }

}
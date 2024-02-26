package com.rick.admin.module.purchase.dao;

import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import com.rick.db.service.support.Params;
import com.rick.db.util.OptionalUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/6/19 16:23
 */
@Repository
public class PurchaseOrderDAO extends EntityCodeDAOImpl<PurchaseOrder, Long> {

    /**
     * 根据code模糊查询 180 天内的采购订单
     * @param keyCode
     * @return
     */
    public Optional<PurchaseOrder> findActivePurchaseOrderByKeyCode(String keyCode) {
        List<PurchaseOrder> purchaseOrderList = selectByParams(Params.builder(1).pv("code", keyCode).build(),
                "code LIKE CONCAT('%',:code) AND TIMESTAMPDIFF(DAY, update_time, now()) < 180");

        if (purchaseOrderList.size() > 1) {
            throw new BizException("系统找到了多个订单号，请输入更多的编号来确定唯一订单号");
        }
        return OptionalUtils.expectedAsOptional(purchaseOrderList);
    }
}
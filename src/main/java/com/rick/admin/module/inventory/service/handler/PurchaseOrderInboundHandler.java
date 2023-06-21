package com.rick.admin.module.inventory.service.handler;

import com.google.common.collect.Lists;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import com.rick.admin.module.purchase.dao.PurchaseOrderItemDAO;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 采购订单入库
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class PurchaseOrderInboundHandler extends AbstractHandler {

    @Resource
    private PurchaseOrderItemDAO purchaseOrderItemDAO;

    @Resource
    PurchaseOrderService purchaseOrderService;

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.INBOUND;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.PO;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        Map<Long, BigDecimal> itemOpenQuantityMap = purchaseOrderService.openQuantity(InventoryDocument.MovementTypeEnum.INBOUND, inventoryDocument.getReferenceCode());
        List<Long> completeIdList = Lists.newArrayListWithExpectedSize(inventoryDocument.getItemList().size());

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(InventoryDocument.MovementTypeEnum.INBOUND);
            item.setRootReferenceCode(item.getReferenceCode());
            item.setRootReferenceItemId(item.getReferenceItemId());
            if (BigDecimalUtils.ge(item.getQuantity(), itemOpenQuantityMap.get(item.getReferenceItemId()))) {
                completeIdList.add(item.getReferenceItemId());
            }
        }

        inventoryDocument.setRootReferenceCode(inventoryDocument.getReferenceCode());

        purchaseOrderItemDAO.setComplete(completeIdList);
    }

}

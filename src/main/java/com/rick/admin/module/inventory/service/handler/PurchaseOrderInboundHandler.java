package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import org.springframework.stereotype.Component;

/**
 * 采购订单入库
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class PurchaseOrderInboundHandler extends AbstractHandler {

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
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(InventoryDocument.MovementTypeEnum.INBOUND);
            item.setRootReferenceCode(item.getReferenceCode());
            item.setRootReferenceItemId(item.getReferenceItemId());
        }

        inventoryDocument.setRootReferenceCode(inventoryDocument.getReferenceCode());
    }

}

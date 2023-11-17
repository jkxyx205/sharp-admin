package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import org.springframework.stereotype.Component;

/**
 * 生产单用料产前库扣减
 * @author Rick.Xu
 * @date 2023/7/12 11:42
 */
@Component
public class ProduceOrderConsumeHandler extends AbstractHandler {

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.CONSUME;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.PP;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(InventoryDocument.MovementTypeEnum.OUTBOUND);
            item.setRootReferenceCode(item.getReferenceCode());
            item.setRootReferenceItemId(item.getReferenceItemId());
        }

        inventoryDocument.setRootReferenceCode(inventoryDocument.getReferenceCode());

    }

}

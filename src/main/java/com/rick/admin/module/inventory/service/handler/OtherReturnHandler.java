package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import org.springframework.stereotype.Component;

/**
 * 退货-无
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class OtherReturnHandler extends AbstractHandler {

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.RETURN;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.OTHER;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(InventoryDocument.MovementTypeEnum.OUTBOUND);
            item.setRootReferenceCode(item.getInventoryDocumentCode());
        }

        inventoryDocument.setRootReferenceCode(inventoryDocument.getCode());
    }

}

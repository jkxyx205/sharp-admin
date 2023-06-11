package com.rick.admin.module.inventory.service;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class OtherOutboundHandler extends AbstractHandler {

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.OUTBOUND;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.OTHER;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setQuantity(BigDecimal.ZERO.subtract(item.getQuantity()));
        }
    }

    @Override
    public InventoryDocument.MovementTypeEnum itemMovementType(InventoryDocument.Item item) {
        return InventoryDocument.MovementTypeEnum.OUTBOUND;
    }

}

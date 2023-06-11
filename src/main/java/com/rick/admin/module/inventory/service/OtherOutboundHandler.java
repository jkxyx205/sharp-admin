package com.rick.admin.module.inventory.service;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import org.springframework.stereotype.Component;

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

    }

    @Override
    public InventoryDocument.MovementTypeEnum itemMovementType(InventoryDocument.Item item) {
        return InventoryDocument.MovementTypeEnum.OUTBOUND;
    }

}

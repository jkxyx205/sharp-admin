package com.rick.admin.module.inventory.service;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import lombok.experimental.UtilityClass;

/**
 * @author Rick.Xu
 * @date 2023/6/11 23:30
 */
@UtilityClass
public class HandlerHelper {

    public InventoryDocument.MovementTypeEnum oppositeMovementType(InventoryDocument.MovementTypeEnum movementType) {
        return movementType == InventoryDocument.MovementTypeEnum.INBOUND ? InventoryDocument.MovementTypeEnum.OUTBOUND :
                InventoryDocument.MovementTypeEnum.INBOUND;
    }
}

package com.rick.admin.module.inventory.service;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class CancelHandler extends AbstractHandler {

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.CANCEL;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.MATERIAL_DOCUMENT;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        Optional<InventoryDocument> optional = inventoryDocumentDAO.selectByCode(inventoryDocument.getReferenceCode());
        InventoryDocument inventoryDocumentInDb = optional.get();
        Map<Long, InventoryDocument.Item> idDocumentMap = inventoryDocumentInDb.getItemList().stream().collect(Collectors.toMap(InventoryDocument.Item::getId, d -> d));

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(HandlerHelper.oppositeMovementType(idDocumentMap.get(item.getReferenceItemId()).getMovementType()));
        }
    }

    @Override
    public InventoryDocument.MovementTypeEnum itemMovementType(InventoryDocument.Item item) {
        return null;
    }

}

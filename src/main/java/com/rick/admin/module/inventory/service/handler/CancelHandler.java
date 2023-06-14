package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.common.http.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class CancelHandler extends DocumentReturnHandler {

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
        super.handle0(inventoryDocument);
    }

    @Override
    public void handle1(InventoryDocument inventoryDocument, InventoryDocument inventoryDocumentInDb, Map<Long, InventoryDocument.Item> idDocumentMap) {
        super.handle1(inventoryDocument, inventoryDocumentInDb, idDocumentMap);

        int affectRow = inventoryDocumentDAO.update("canceled", new Object[]{true, inventoryDocumentInDb.getId()}, "id = ? AND canceled = 0");
        if (affectRow == 0) {
            throw new BizException(ExceptionCodeEnum.MATERIAL_DOCUMENT_IS_CANCELED_ERROR, new Object[]{inventoryDocument.getReferenceCode()});
        }

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            InventoryDocument.Item itemInDb = idDocumentMap.get(item.getReferenceItemId());
            item.setQuantity(itemInDb.getQuantity());
        }

    }

}

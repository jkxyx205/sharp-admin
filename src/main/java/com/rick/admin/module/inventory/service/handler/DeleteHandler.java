package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.common.http.exception.BizException;
import com.rick.db.service.support.Params;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class DeleteHandler extends DocumentReturnHandler {

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.DELETE;
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

        int affectRow = inventoryDocumentDAO.update("canceled, is_deleted", new Object[]{true, true, inventoryDocumentInDb.getId()}, "id = ? AND canceled = 0");
        if (affectRow == 0) {
            throw new BizException(ExceptionCodeEnum.MATERIAL_DOCUMENT_IS_CANCELED_ERROR, new Object[]{inventoryDocument.getReferenceCode()});
        }

        inventoryDocument.setDeleted(true);
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            InventoryDocument.Item itemInDb = idDocumentMap.get(item.getReferenceItemId());
            item.setQuantity(itemInDb.getQuantity());
            item.setDeleted(true);
        }

        inventoryDocumentItemDAO.update("is_deleted",
                Params.builder(2).pv("ids", inventoryDocumentInDb.getItemList().stream().map(InventoryDocument.Item::getId).collect(Collectors.toSet())).pv("is_deleted", true).build(), "id IN (:ids) AND is_deleted = 0");

    }

}

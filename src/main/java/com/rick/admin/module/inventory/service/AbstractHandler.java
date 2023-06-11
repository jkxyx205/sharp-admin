package com.rick.admin.module.inventory.service;

import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.entity.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@RequiredArgsConstructor
public abstract class AbstractHandler implements MovementHandler {

    @Resource
    protected InventoryDocumentDAO inventoryDocumentDAO;

    @Resource
    protected StockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(InventoryDocument inventoryDocument) {
        inventoryDocument.setCode(CodeHelper.generateCode("MD"));

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setReferenceType(inventoryDocument.getReferenceType());
            item.setReferenceCode(inventoryDocument.getReferenceCode());
            item.setPlantId(inventoryDocument.getPlantId());
            item.setMovementType(itemMovementType(item));
        }

        handle0(inventoryDocument);
        inventoryDocumentDAO.insert(inventoryDocument);

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            stockService.changeStockQuantity(Stock.builder()
                    .plantId(item.getPlantId())
                    .materialId(item.getMaterialId())
                    .quantity(item.getQuantity())
                    .unit(item.getUnit())
                    .build());
        }
    }

    public abstract void handle0(InventoryDocument inventoryDocument);

    public abstract InventoryDocument.MovementTypeEnum itemMovementType(InventoryDocument.Item item);

}

package com.rick.admin.module.inventory.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.entity.Stock;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.stream.Collectors;

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
    public void handle(InventoryDocument inventoryDocument) {
        inventoryDocument.setCode(CodeHelper.generateCode("MD"));
        inventoryDocument.setCanceled(false);

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setId(null);
            item.setType(inventoryDocument.getType());
            item.setReferenceType(inventoryDocument.getReferenceType());
            item.setReferenceCode(inventoryDocument.getReferenceCode());
            item.setInventoryDocumentCode(inventoryDocument.getCode());
            item.setPlantId(inventoryDocument.getPlantId());
        }

        handle0(inventoryDocument);
        inventoryDocument.setItemList(inventoryDocument.getItemList().stream().filter(item -> BigDecimalUtils.gt(item.getQuantity(), BigDecimal.ZERO)).collect(Collectors.toList()));

        if (CollectionUtils.isEmpty(inventoryDocument.getItemList())) {
            inventoryDocument.setCode(null);
            return;
        }

        inventoryDocumentDAO.insert(inventoryDocument);

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            stockService.changeStockQuantity(Stock.builder()
                    .plantId(item.getPlantId())
                    .materialId(item.getMaterialId())
                    .quantity(item.getMovementType() == InventoryDocument.MovementTypeEnum.OUTBOUND ? BigDecimal.ZERO.subtract(item.getQuantity()) : item.getQuantity())
                    .unit(item.getUnit())
                    .build());
        }
    }

    public abstract void handle0(InventoryDocument inventoryDocument);

}

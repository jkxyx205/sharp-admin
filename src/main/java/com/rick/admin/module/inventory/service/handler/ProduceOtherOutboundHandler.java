package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 领料
 * @author Rick.Xu
 * @date 2023/7/12 11:42
 */
@Component
public class ProduceOtherOutboundHandler extends AbstractHandler {

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.MOVING_TO_PRODUCE;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.OTHER;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        List<InventoryDocument.Item> inboundItemList = new ArrayList<>();

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(InventoryDocument.MovementTypeEnum.OUTBOUND);
            item.setRootReferenceCode(item.getInventoryDocumentCode());

            InventoryDocument.Item newItem = new InventoryDocument.Item();
            BeanUtils.copyProperties(item, newItem);
            newItem.setMovementType(InventoryDocument.MovementTypeEnum.INBOUND);
            newItem.setPlantId(726158903766683648L);
            inboundItemList.add(newItem);
        }

        inventoryDocument.getItemList().addAll(inboundItemList);
        inventoryDocument.setRootReferenceCode(inventoryDocument.getCode());
    }

}

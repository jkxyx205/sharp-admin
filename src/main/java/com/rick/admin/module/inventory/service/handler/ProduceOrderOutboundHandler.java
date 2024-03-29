package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import com.rick.admin.module.produce.service.ProduceScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 生产单领料
 * @author Rick.Xu
 * @date 2023/7/12 11:42
 */
@Component
public class ProduceOrderOutboundHandler extends AbstractHandler {

    @Resource
    ProduceScheduleService produceScheduleService;

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.MOVING_TO_PRODUCE;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.PP;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
//        Map<Long, BigDecimal> itemOpenQuantityMap = produceScheduleService.openQuantity(InventoryDocument.MovementTypeEnum.OUTBOUND, inventoryDocument.getReferenceCode());
//        List<Long> completeIdList = Lists.newArrayListWithExpectedSize(inventoryDocument.getItemList().size());

        List<InventoryDocument.Item> inboundItemList = new ArrayList<>();

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(InventoryDocument.MovementTypeEnum.OUTBOUND);
            item.setRootReferenceCode(item.getReferenceCode());
            item.setRootReferenceItemId(item.getReferenceItemId());

//            if (BigDecimalUtils.ge(item.getQuantity(), itemOpenQuantityMap.get(item.getReferenceItemId()))) {
//                completeIdList.add(item.getReferenceItemId());
//            }

//            itemOpenQuantityMap.put(item.getReferenceItemId(), itemOpenQuantityMap.get(item.getReferenceItemId()).subtract(item.getQuantity()));

            InventoryDocument.Item newItem = new InventoryDocument.Item();
            BeanUtils.copyProperties(item, newItem);
            newItem.setMovementType(InventoryDocument.MovementTypeEnum.INBOUND);
            newItem.setPlantId(726158903766683648L);
            inboundItemList.add(newItem);
        }

        inventoryDocument.getItemList().addAll(inboundItemList);
        inventoryDocument.setRootReferenceCode(inventoryDocument.getReferenceCode());

//        boolean complete = itemOpenQuantityMap.values().stream().allMatch(quantity -> BigDecimalUtils.le(quantity, BigDecimal.ZERO));
//        if (complete) {
//            produceOrderService.setProcessingStatus(inventoryDocument.getRootReferenceCode());
//        }
//
//        produceOrderService.setProcessingComplete(completeIdList);

    }

}

package com.rick.admin.module.inventory.service.handler;

import com.google.common.collect.Lists;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import com.rick.admin.module.produce.service.ProduceOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 生产单出库
 * @author Rick.Xu
 * @date 2023/7/12 11:42
 */
@Component
public class ProduceOrderOutboundHandler extends AbstractHandler {

    @Resource
    ProduceOrderService produceOrderService;

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.OUTBOUND;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.PDO;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        Map<Long, BigDecimal> itemOpenQuantityMap = produceOrderService.openQuantity(InventoryDocument.MovementTypeEnum.OUTBOUND, inventoryDocument.getReferenceCode());
        List<Long> completeIdList = Lists.newArrayListWithExpectedSize(inventoryDocument.getItemList().size());

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(InventoryDocument.MovementTypeEnum.OUTBOUND);
            item.setRootReferenceCode(item.getReferenceCode());
            item.setRootReferenceItemId(item.getReferenceItemId());

            if (BigDecimalUtils.ge(item.getQuantity(), itemOpenQuantityMap.get(item.getReferenceItemId()))) {
                completeIdList.add(item.getReferenceItemId());
            }

            itemOpenQuantityMap.put(item.getReferenceItemId(), itemOpenQuantityMap.get(item.getReferenceItemId()).subtract(item.getQuantity()));
        }

        inventoryDocument.setRootReferenceCode(inventoryDocument.getReferenceCode());

        boolean complete = itemOpenQuantityMap.values().stream().allMatch(quantity -> BigDecimalUtils.le(quantity, BigDecimal.ZERO));
        if (complete) {
            produceOrderService.setComplete(inventoryDocument.getRootReferenceCode());
        }

        produceOrderService.setComplete(completeIdList);
    }

}

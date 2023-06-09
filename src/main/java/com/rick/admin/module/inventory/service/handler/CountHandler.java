package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class CountHandler extends AbstractHandler {

    @Resource
    private StockDAO stockDAO;

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.COUNT;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.OTHER;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        Map<Long, BigDecimal> materialIdStockQuantityMap = stockDAO.getStockQuantityByMaterialId(inventoryDocument.getItemList().stream().map(InventoryDocument.Item::getMaterialId).collect(Collectors.toSet()), inventoryDocument.getPlantId());

        Iterator<InventoryDocument.Item> iterator = inventoryDocument.getItemList().iterator();
        while (iterator.hasNext()) {
            InventoryDocument.Item item = iterator.next();
            BigDecimal quantityInDb = ObjectUtils.defaultIfNull(materialIdStockQuantityMap.get(item.getMaterialId()), BigDecimal.ZERO);
            item.setRootReferenceCode(item.getInventoryDocumentCode());

            BigDecimal difference = item.getQuantity().subtract(quantityInDb);
            item.setRemark(item.getRemark() + " " + "盘点数量："+item.getQuantity()+"，差异：" + difference);

            if(BigDecimalUtils.gt(difference, BigDecimal.ZERO)) {
                item.setMovementType(InventoryDocument.MovementTypeEnum.INBOUND);
                item.setQuantity(difference);
            } else if(BigDecimalUtils.lt(difference, BigDecimal.ZERO)) {
                item.setQuantity(BigDecimal.ZERO.subtract(difference));
                item.setMovementType(InventoryDocument.MovementTypeEnum.OUTBOUND);
            } else {
                iterator.remove();
            }
        }

        inventoryDocument.setRootReferenceCode(inventoryDocument.getCode());
    }

}

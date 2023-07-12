package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.ProduceOrderService;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/7/12 15:42
 */
@Component
public class ProduceOrderReturnHandler extends AbstractHandler {

    @Resource
    MaterialDAO materialDAO;

    @Resource
    EntityCodeDAO<ProduceOrder, Long> produceOrderDAO;

//    @Resource
//    PurchaseOrderItemDAO purchaseOrderItemDAO;

    @Resource
    ProduceOrderService produceOrderService;

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.RETURN;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.PDO;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        Optional<ProduceOrder> optional = produceOrderDAO.selectByCode(inventoryDocument.getReferenceCode());
        if (!optional.isPresent()) {
            throw new BizException(ExceptionCodeEnum.PDO_DOCUMENT_NOT_FOUND_ERROR, new Object[]{inventoryDocument.getReferenceCode()});
        }

        ProduceOrder produceOrder = optional.get();

        inventoryDocument.setRootReferenceCode(produceOrder.getCode());
        InventoryDocument.MovementTypeEnum movementType = ObjectUtils.defaultIfNull(inventoryDocument.getItemList().get(0).getMovementType(), InventoryDocument.MovementTypeEnum.INBOUND);

        Map<Long, BigDecimal> itemOpenQuantityMap = produceOrderService.openQuantity(movementType, produceOrder.getCode());

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(ObjectUtils.defaultIfNull(item.getMovementType(), InventoryDocument.MovementTypeEnum.INBOUND));
            item.setRootReferenceCode(produceOrder.getCode());
            item.setRootReferenceItemId(ObjectUtils.defaultIfNull(item.getRootReferenceItemId(), item.getReferenceItemId()));
            checkOpenQuantity(item.getMaterialId(), item.getQuantity(), itemOpenQuantityMap.get(item.getRootReferenceItemId()));
        }

        boolean complete = itemOpenQuantityMap.values().stream().allMatch(quantity -> BigDecimalUtils.le(quantity, BigDecimal.ZERO));
        if (complete) {
            produceOrderService.setComplete(inventoryDocument.getRootReferenceCode());
        }
    }

    private void checkOpenQuantity(Long materialId, BigDecimal movementQuantity, BigDecimal openQuantity) {
        if (BigDecimalUtils.gt(movementQuantity, openQuantity)) {
            throw new BizException(ExceptionCodeEnum.MATERIAL_OVER_MAX_MOVEMENT_ERROR, new Object[]{materialDAO.selectCodeById(materialId).get()});
        }
    }

}

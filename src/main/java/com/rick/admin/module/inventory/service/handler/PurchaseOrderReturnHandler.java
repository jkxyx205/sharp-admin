package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import com.rick.common.http.exception.BizException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class PurchaseOrderReturnHandler extends AbstractHandler {

    @Resource
    MaterialDAO materialDAO;

    @Resource
    PurchaseOrderDAO purchaseOrderDAO;

    @Resource
    PurchaseOrderService purchaseOrderService;

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.RETURN;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.PO;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        Optional<PurchaseOrder> optional = purchaseOrderDAO.selectByCode(inventoryDocument.getReferenceCode());
        if (!optional.isPresent()) {
            throw new BizException(ExceptionCodeEnum.PO_DOCUMENT_NOT_FOUND_ERROR, new Object[]{inventoryDocument.getReferenceCode()});
        }

        PurchaseOrder purchaseOrder = optional.get();

        inventoryDocument.setRootReferenceCode(purchaseOrder.getCode());

        Map<Long, BigDecimal> itemOpenQuantityMap = purchaseOrderService.openQuantity(inventoryDocument.getItemList().get(0).getMovementType(), purchaseOrder.getCode());

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(ObjectUtils.defaultIfNull(item.getMovementType(), InventoryDocument.MovementTypeEnum.OUTBOUND));
            item.setRootReferenceCode(purchaseOrder.getCode());
            item.setRootReferenceItemId(ObjectUtils.defaultIfNull(item.getRootReferenceItemId(), item.getReferenceItemId()));
            checkOpenQuantity(item.getMaterialId(), item.getQuantity(), itemOpenQuantityMap.get(item.getRootReferenceItemId()));
        }
    }

    private void checkOpenQuantity(Long materialId, BigDecimal movementQuantity, BigDecimal openQuantity) {
        if (BigDecimalUtils.gt(movementQuantity, openQuantity)) {
            throw new BizException(ExceptionCodeEnum.MATERIAL_OVER_MAX_MOVEMENT_ERROR, new Object[]{materialDAO.selectCodeById(materialId).get()});
        }
    }

}

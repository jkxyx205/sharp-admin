package com.rick.admin.module.inventory.service.handler;

import com.google.common.collect.Lists;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.common.model.IdQuantity;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import com.rick.common.http.exception.BizException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class PurchaseOrderReturnHandler extends AbstractHandler {

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

        InventoryDocument.MovementTypeEnum movementType = ObjectUtils.defaultIfNull(inventoryDocument.getItemList().get(0).getMovementType(), InventoryDocument.MovementTypeEnum.OUTBOUND);

        Map<Long, BigDecimal> itemOpenQuantityMap = purchaseOrderService.openQuantity(movementType, purchaseOrder.getCode());
        Map<Long, BigDecimal> itemOrderQuantityMap = purchaseOrderService.itemOrderQuantity(inventoryDocument.getReferenceCode());

        List<Long> completedIdList = Lists.newArrayListWithExpectedSize(inventoryDocument.getItemList().size());
        List<Long> uncompletedIdList = Lists.newArrayListWithExpectedSize(inventoryDocument.getItemList().size());
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(movementType);
            item.setRootReferenceCode(purchaseOrder.getCode());
            item.setRootReferenceItemId(ObjectUtils.defaultIfNull(item.getRootReferenceItemId(), item.getReferenceItemId()));

            if (item.getMovementType() == InventoryDocument.MovementTypeEnum.INBOUND && BigDecimalUtils.ge(item.getQuantity(), itemOpenQuantityMap.get(item.getRootReferenceItemId()))) {
                completedIdList.add(item.getRootReferenceItemId());
            } else if (BigDecimalUtils.lt(itemOpenQuantityMap.get(item.getRootReferenceItemId()).subtract(item.getQuantity()), itemOrderQuantityMap.get(item.getRootReferenceItemId()))){
                uncompletedIdList.add(item.getRootReferenceItemId());
            }
        }

        purchaseOrderService.checkItemOpenQuantity(movementType, inventoryDocument.getReferenceCode()
                , inventoryDocument.getItemList().stream().map(item -> new IdQuantity(item.getRootReferenceItemId(), item.getMaterialCode(), item.getQuantity())).collect(Collectors.toList()));

        purchaseOrderService.setItemCompleteStatus(completedIdList, uncompletedIdList, inventoryDocument.getReferenceCode());
    }


}

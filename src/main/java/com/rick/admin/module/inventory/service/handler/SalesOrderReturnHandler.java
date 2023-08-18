package com.rick.admin.module.inventory.service.handler;

import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class SalesOrderReturnHandler extends AbstractHandler {

    @Resource
    MaterialDAO materialDAO;

    @Resource
    EntityCodeDAO<ProduceOrder, Long> produceOrderDAO;

    @Resource
    ProduceOrderService produceOrderService;

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.RETURN;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.SO;
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

        Map<Long, BigDecimal> itemOpenQuantityMap = produceOrderService.saleOpenQuantity(movementType, produceOrder.getCode());

        List<Long> completeIdList = Lists.newArrayListWithExpectedSize(inventoryDocument.getItemList().size());
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(ObjectUtils.defaultIfNull(item.getMovementType(), InventoryDocument.MovementTypeEnum.INBOUND));
            item.setRootReferenceCode(produceOrder.getCode());
            item.setRootReferenceItemId(ObjectUtils.defaultIfNull(item.getRootReferenceItemId(), item.getReferenceItemId()));
            checkOpenQuantity(item.getMaterialId(), item.getQuantity(), itemOpenQuantityMap.get(item.getRootReferenceItemId()));

            if (item.getMovementType() == InventoryDocument.MovementTypeEnum.OUTBOUND && BigDecimalUtils.ge(item.getQuantity(), itemOpenQuantityMap.get(item.getRootReferenceItemId()))) {
                completeIdList.add(item.getRootReferenceItemId());
            }
        }

        produceOrderService.setIssueComplete(completeIdList);
    }

    private void checkOpenQuantity(Long materialId, BigDecimal movementQuantity, BigDecimal openQuantity) {
        if (BigDecimalUtils.gt(movementQuantity, openQuantity)) {
            throw new BizException(ExceptionCodeEnum.MATERIAL_OVER_MAX_MOVEMENT_ERROR, new Object[]{materialDAO.selectCodeById(materialId).get()});
        }
    }

}

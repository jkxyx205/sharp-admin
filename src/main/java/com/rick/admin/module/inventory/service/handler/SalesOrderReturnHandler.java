package com.rick.admin.module.inventory.service.handler;

import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Optional;

/**
 * 生产单退货
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
            throw new BizException(ExceptionCodeEnum.SO_DOCUMENT_NOT_FOUND_ERROR, new Object[]{inventoryDocument.getReferenceCode()});
        }

        ProduceOrder produceOrder = optional.get();

        inventoryDocument.setRootReferenceCode(produceOrder.getCode());

        List<Long> unCompleteIdList = Lists.newArrayListWithExpectedSize(inventoryDocument.getItemList().size());
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setMovementType(ObjectUtils.defaultIfNull(item.getMovementType(), InventoryDocument.MovementTypeEnum.INBOUND));
            item.setRootReferenceCode(produceOrder.getCode());
            item.setRootReferenceItemId(ObjectUtils.defaultIfNull(item.getRootReferenceItemId(), item.getReferenceItemId()));
            unCompleteIdList.add(item.getRootReferenceItemId());
        }

        produceOrderService.setIssueUnComplete(unCompleteIdList);
        produceOrderService.setStatus(inventoryDocument.getRootReferenceCode(), ProduceOrder.StatusEnum.PRODUCED);
    }

}

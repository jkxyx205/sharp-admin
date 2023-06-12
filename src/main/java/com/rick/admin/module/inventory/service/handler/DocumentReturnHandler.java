package com.rick.admin.module.inventory.service.handler;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.ExceptionCodeEnum;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.AbstractHandler;
import com.rick.admin.module.inventory.service.HandlerHelper;
import com.rick.admin.module.inventory.service.InventoryDocumentService;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.common.http.exception.BizException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@Component
public class DocumentReturnHandler extends AbstractHandler {

    @Resource
    InventoryDocumentService inventoryDocumentService;

    @Resource
    MaterialDAO materialDAO;

    @Override
    public InventoryDocument.TypeEnum type() {
        return InventoryDocument.TypeEnum.RETURN;
    }

    @Override
    public InventoryDocument.ReferenceTypeEnum reference() {
        return InventoryDocument.ReferenceTypeEnum.MATERIAL_DOCUMENT;
    }

    @Override
    public void handle0(InventoryDocument inventoryDocument) {
        Optional<InventoryDocument> optional = inventoryDocumentDAO.selectByCode(inventoryDocument.getReferenceCode());

        if (!optional.isPresent()) {
            throw new BizException(ExceptionCodeEnum.MATERIAL_DOCUMENT_NOT_FOUND_ERROR, new Object[]{inventoryDocument.getReferenceCode()});
        }

        InventoryDocument inventoryDocumentInDb = optional.get();

        inventoryDocument.setRootReferenceCode(StringUtils.defaultString(inventoryDocumentInDb.getRootReferenceCode(), inventoryDocument.getReferenceCode()));

        Map<Long, BigDecimal> itemOpenQuantityMap = inventoryDocumentService.openQuantity(HandlerHelper.oppositeMovementType(inventoryDocumentInDb.getItemList().get(0).getMovementType()),
                inventoryDocumentInDb.getRootReferenceCode());

        Map<Long, InventoryDocument.Item> idDocumentMap = inventoryDocumentInDb.getItemList().stream().collect(Collectors.toMap(InventoryDocument.Item::getId, d -> d));

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            InventoryDocument.Item itemInDb = idDocumentMap.get(item.getReferenceItemId());
            item.setMovementType(HandlerHelper.oppositeMovementType(itemInDb.getMovementType()));
            item.setRootReferenceCode(StringUtils.defaultString(itemInDb.getRootReferenceCode(), itemInDb.getReferenceCode()));
            item.setRootReferenceItemId(ObjectUtils.defaultIfNull(itemInDb.getRootReferenceItemId(), itemInDb.getId()));

            checkOpenQuantity(item.getMaterialId(), item.getQuantity(), itemOpenQuantityMap.get(item.getRootReferenceItemId()));
        }

        handle1(inventoryDocument, inventoryDocumentInDb, idDocumentMap);
    }

    public void handle1(InventoryDocument inventoryDocument, InventoryDocument inventoryDocumentInDb, Map<Long, InventoryDocument.Item> idDocumentMap) {

    }

    private void checkOpenQuantity(Long materialId, BigDecimal movementQuantity, BigDecimal openQuantity) {
        if (BigDecimalUtils.gt(movementQuantity, openQuantity)) {
            throw new BizException(ExceptionCodeEnum.MATERIAL_OVER_MAX_MOVEMENT_ERROR, new Object[]{materialDAO.selectCodeById(materialId).get()});
        }
    }

}

package com.rick.admin.module.inventory.service;

import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/6/12 09:37
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class InventoryDocumentService {

    InventoryDocumentDAO inventoryDocumentDAO;

    SharpService sharpService;

    /**
     * 获取物料凭证 open 的数量
     * @param movementType
     * @param rootReferenceCode
     * @return
     */
    public Map<Long, BigDecimal> openQuantity(InventoryDocument.MovementTypeEnum movementType, String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode group by root_reference_item_id";
        Map<Long, BigDecimal> maxReturnQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        Optional<InventoryDocument> optional = inventoryDocumentDAO.selectByCode(rootReferenceCode);
        InventoryDocument.MovementTypeEnum originMovementType = InventoryDocument.MovementTypeEnum.valueOf(optional.get().getType().name());
        if (movementType == originMovementType) {
            // 同向
            for (InventoryDocument.Item item : optional.get().getItemList()) {
                maxReturnQuantityMap.put(item.getRootReferenceItemId(), item.getQuantity().subtract(maxReturnQuantityMap.get(item.getRootReferenceItemId())));
            }
        }

        return maxReturnQuantityMap;
    }

}
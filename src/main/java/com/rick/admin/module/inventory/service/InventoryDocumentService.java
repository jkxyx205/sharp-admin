package com.rick.admin.module.inventory.service;

import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.common.util.JsonUtils;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Map<Long, BigDecimal> openQuantity(InventoryDocument.MovementTypeEnum movementType, String inventoryCode, String rootReferenceCode) {
        Assert.hasLength(rootReferenceCode, "rootReferenceCode cannot be blank");
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode group by root_reference_item_id";
        Map<Long, BigDecimal> maxReturnQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        Optional<InventoryDocument> optional = inventoryDocumentDAO.selectByCode(inventoryCode);

        for (InventoryDocument.Item item : optional.get().getItemList()) {
            if (movementType == item.getMovementType()) {
                // 同向
                maxReturnQuantityMap.put(item.getRootReferenceItemId(), item.getQuantity().subtract(maxReturnQuantityMap.get(item.getRootReferenceItemId())));
            }
        }

        return maxReturnQuantityMap;
    }

    public void updateInventoryDocumentByCode(String code, List<Map<String, Object>> attachmentList, String remark, Map<Long, String> itemIdRemarkMap) {
        inventoryDocumentDAO.update(" attachment, remark", new Object[]{JsonUtils.toJson(attachmentList), remark, code}, "code = ?");
        if (MapUtils.isNotEmpty(itemIdRemarkMap)) {
            SQLUtils.update("inv_document_item", "remark",
                    itemIdRemarkMap.entrySet().stream().map(entry -> new Object[]{entry.getValue(), entry.getKey()}).collect(Collectors.toList()),
                    "id = ?");
        }
    }

}
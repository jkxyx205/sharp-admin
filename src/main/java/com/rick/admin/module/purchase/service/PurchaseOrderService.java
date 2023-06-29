package com.rick.admin.module.purchase.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/20 13:39
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class PurchaseOrderService {

    PurchaseOrderDAO purchaseOrderDAO;

    SharpService sharpService;

    /**
     * 新增或修改
     * @param order
     */
    @ResponseBody
    public void saveOrUpdate(@RequestBody PurchaseOrder order) {
        if (order.getId() == null) {
            order.setCode(CodeHelper.generateCode("PO"));
        }

        order.getItemList().forEach(item -> {
            item.setComplete(false);
            item.setPurchaseOrderCode(order.getCode());
        });

        purchaseOrderDAO.insertOrUpdate(order);
    }

    /**
     * 获取采购订单 open 的数量
     * @param movementType
     * @param rootReferenceCode
     * @return
     */
    public Map<Long, BigDecimal> openQuantity(InventoryDocument.MovementTypeEnum movementType, String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        if (movementType == InventoryDocument.MovementTypeEnum.INBOUND) {
            // 同向
            PurchaseOrder purchaseOrder = purchaseOrderDAO.selectByCode(rootReferenceCode).orElseThrow(() -> new ResourceNotFoundException());

            for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
                BigDecimal value = item.getQuantity().subtract(ObjectUtils.defaultIfNull(histroyGoodsReceiptQuantityMap.get(item.getId()), BigDecimal.ZERO));
                histroyGoodsReceiptQuantityMap.put(item.getId(), BigDecimalUtils.lt(value, BigDecimal.ZERO) ? BigDecimal.ZERO : value);
            }
        }

        return histroyGoodsReceiptQuantityMap;
    }

    /**
     * 历史收货数量
     * @param rootReferenceCode
     * @return
     */
    public Map<Long, BigDecimal> historyGoodsReceiptQuantity(String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        PurchaseOrder purchaseOrder = purchaseOrderDAO.selectByCode(rootReferenceCode).orElseThrow(() -> new ResourceNotFoundException());

        for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
            histroyGoodsReceiptQuantityMap.put(item.getId(), ObjectUtils.defaultIfNull(histroyGoodsReceiptQuantityMap.get(item.getId()), BigDecimal.ZERO));
        }

        return histroyGoodsReceiptQuantityMap;
    }

}
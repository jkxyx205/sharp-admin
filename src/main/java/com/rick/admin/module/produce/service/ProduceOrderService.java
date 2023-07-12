package com.rick.admin.module.produce.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/7/11 11:54
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class ProduceOrderService {

    EntityCodeDAO<ProduceOrder, Long> produceOrderDAO;

    SharpService sharpService;

    /**
     * 新增或修改
     * @param order
     */
    public void saveOrUpdate(ProduceOrder order) {
        if (order.getId() == null && StringUtils.isBlank(order.getCode())) {
            order.setCode(CodeHelper.generateCode("PDO"));
        }

        order.getItemList().forEach(item -> {
            item.setProduceOrderCode(order.getCode());
        });

        produceOrderDAO.insertOrUpdate(order);
    }

    /**
     * 获取生产订单 open 的数量
     * @param movementType
     * @param rootReferenceCode
     * @return
     */
    public Map<Long, BigDecimal> openQuantity(InventoryDocument.MovementTypeEnum movementType, String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        String sql2 = "select produce_bom_detail.`material_id`, sum(produce_bom_detail.quantity * produce_order_item.quantity) quantity from produce_order \n" +
                "inner join produce_order_item on produce_order_item.`produce_order_id` = produce_order.id \n" +
                "inner join produce_bom on produce_bom.`material_id` = produce_order_item.material_id\n" +
                "inner join produce_bom_detail on produce_bom.id = produce_bom_detail.`bom_id`\n" +
                "inner join mm_material on mm_material.id = produce_bom_detail.`material_id`\n" +
                "where produce_order.code = :rootReferenceCode group by material_id";

        Map<Long, BigDecimal> requestQuantityMap = sharpService.queryForKeyValue(sql2, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        if (movementType == InventoryDocument.MovementTypeEnum.OUTBOUND) {
            // 同向
            for (Map.Entry<Long, BigDecimal> entry : requestQuantityMap.entrySet()) {
                BigDecimal value = entry.getValue().subtract(ObjectUtils.defaultIfNull(histroyGoodsReceiptQuantityMap.get(entry.getKey()), BigDecimal.ZERO));
                histroyGoodsReceiptQuantityMap.put(entry.getKey(), BigDecimalUtils.lt(value, BigDecimal.ZERO) ? BigDecimal.ZERO : value);
            }
        }

        return histroyGoodsReceiptQuantityMap;
    }

    public void setComplete(String rootReferenceCode) {
        produceOrderDAO.update("status", new Object[]{ProduceOrder.StatusEnum.PROCESSING.getCode(), rootReferenceCode}, "code = ?");
    }

}
package com.rick.admin.module.purchase.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.dao.PurchaseOrderItemDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.entity.Dict;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    PurchaseOrderItemDAO purchaseOrderItemDAO;

    /**
     * 新增或修改
     * @param order
     */
    public void saveOrUpdate(PurchaseOrder order) {
        if (order.getId() == null) {
            order.setCode(CodeHelper.generateCode("PO"));
        }

        order.getItemList().forEach(item -> {
            item.setPurchaseOrderCode(order.getCode());
        });

        purchaseOrderDAO.insertOrUpdate(order);
    }

    public void save(List<PurchaseOrder> list) {
        for (int i = 0; i < list.size(); i++) {
            PurchaseOrder purchaseOrder = list.get(i);
            if (list.size() == 1) {
                purchaseOrder.setCode(CodeHelper.generateCode("PO"));
            } else {
                purchaseOrder.setCode(CodeHelper.generateCode("PO") + "-" + (i + 1));
            }

            purchaseOrder.getItemList().forEach(item -> {
                item.setComplete(false);
                item.setPurchaseOrderCode(purchaseOrder.getCode());
            });
        }
        purchaseOrderDAO.insert(list);
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

    /**
     * 获取物料的供应商
     * @param materialId
     * @return
     */
    public List<Dict> getVendorByMaterialId(Long materialId) {
        String sql = "select id name, name label from core_partner where `partner_type` = 'VENDOR' and exists(select 1 from pur_source_list left join mm_material on mm_material.`category_id` = pur_source_list.`material_category_id` where (mm_material.id = :materialId or `material_id` = :materialId) AND core_partner.id = partner_id)";
        return sharpService.query(sql, Params.builder(1).pv("materialId", materialId).build(), Dict.class);
    }

    /**
     * 批量获取物料的供应商
     * @param materialIds
     * @return
     */
    public Map<String, List<Dict>> getVendorByMaterialIds(Collection<Long> materialIds) {
        String sql = "select sl.material_id, core_partner.id name, core_partner.name label from core_partner inner join (select `partner_id`, ifnull(`material_id`, mm_material.id) material_id from pur_source_list left join mm_material on mm_material.`category_id` = pur_source_list.`material_category_id` where mm_material.id IN (:materialIds) or `material_id` IN (:materialIds)) sl on sl.partner_id = core_partner.id where `partner_type` = 'VENDOR'";
        List<Map<String, Object>> list = sharpService.query(sql, Params.builder(1).pv("materialIds", materialIds).build());
        Map<Long, List<Map<String, Object>>> map = list.stream().collect(Collectors.groupingBy(m -> (Long) m.get("material_id")));
        Map<String, List<Dict>> resultMap = Maps.newHashMapWithExpectedSize(map.size());
        map.forEach((k, v) -> {
            List<Dict> resultList = Lists.newArrayListWithExpectedSize(v.size());
            for (Map<String, Object> option : v) {
                resultList.add(new Dict(null, String.valueOf(option.get("name")), (String) option.get("label"), null));
            }
            resultMap.put(String.valueOf(k), resultList);

        });
        return resultMap;
    }

    public void ifAllCompleteAndSetDone(String rootReferenceCode) {
        List<PurchaseOrder.Item> list = purchaseOrderItemDAO.list(rootReferenceCode);

        boolean hasUnComplete = list.stream().anyMatch(item -> !item.getComplete());
        if (!hasUnComplete) {
            purchaseOrderDAO.update("status"
                    , Params.builder(2).pv("code", rootReferenceCode).pv("status", PurchaseOrder.StatusEnum.DONE).build(),
                    "code = :code");
        }
    }
}
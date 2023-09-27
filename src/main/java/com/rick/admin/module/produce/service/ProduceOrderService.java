package com.rick.admin.module.produce.service;

import com.google.common.collect.Lists;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.core.model.ReferenceTypeEnum;
import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.produce.dao.ProduceOrderItemDAO;
import com.rick.admin.module.produce.dao.ProduceOrderItemDetailDAO;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.purchase.entity.PurchaseRequisition;
import com.rick.admin.module.purchase.service.PurchaseRequisitionItemService;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    ProduceOrderItemDetailDAO produceOrderItemDetailDAO;

    ProduceOrderItemDAO produceOrderItemDAO;

    SharpService sharpService;

    BatchService batchService;

    PurchaseRequisitionItemService purchaseRequisitionItemService;

    /**
     * 新增或修改
     *
     * @param order
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(ProduceOrder order) {
        if (order.getId() == null) {
            order.setCode(CodeHelper.generateCode("SO"));
        }

        AtomicInteger atomicInteger = new AtomicInteger(0);
        order.getItemList().forEach(item -> {
            item.setProduceOrderCode(order.getCode());

            if (Objects.isNull(item.getId())) {
                item.setComplete(false);
            }

            if (order.getStatus() == ProduceOrder.StatusEnum.DONE) {
                item.setComplete(true);
            }

            for (ProduceOrder.Item.Schedule schedule : item.getScheduleList()) {
                if (order.getStatus() == ProduceOrder.StatusEnum.PRODUCED || order.getStatus() == ProduceOrder.StatusEnum.DONE || item.getComplete()) {
                    schedule.setStatus(ProduceOrder.StatusEnum.PRODUCED);
                }

                if (Objects.isNull(schedule.getId())) {
                    schedule.setCode(CodeHelper.generateCode("PP") + (atomicInteger.incrementAndGet()));
                }
            }

        });

        if (order.getItemList().stream().allMatch(item -> item.getComplete())) {
            order.setStatus(ProduceOrder.StatusEnum.DONE);
        }

        batchService.saveBatch(Stream.concat(order.getItemList().stream(), order.getItemList().stream().flatMap(item -> CollectionUtils.isNotEmpty(item.getItemList()) ? item.getItemList().stream() : Stream.empty())).collect(Collectors.toSet()));
        produceOrderDAO.insertOrUpdate(order);

        if (order.getStatus() == ProduceOrder.StatusEnum.PRODUCING) {
            handlePurchaseSendType(order.getItemList().stream().filter(item -> item.getItemCategory() == ProduceOrder.ItemCategoryEnum.PURCHASE_SEND).collect(Collectors.toList()));
        }
    }

    /**
     * 获取生产订单 open 的数量
     *
     * @param movementType
     * @param rootReferenceCode
     * @return
     */
    public Map<Long, BigDecimal> openQuantity(InventoryDocument.MovementTypeEnum movementType, String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode AND plant_id = 719893335619162112 group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        String sql2 = "select produce_order_item.id, produce_order_item.quantity  from produce_order_item\n" +
                "inner join mm_material on mm_material.id = material_id where produce_order_item.`produce_order_code` = :rootReferenceCode AND mm_material.material_type = 'ROH'\n" +
                "UNION ALL " +
                "select produce_order_item_detail.`id`, produce_order_item_detail.quantity * produce_order_item.quantity from produce_order\n" +
                "                inner join produce_order_item on produce_order_item.`produce_order_id` = produce_order.id\n" +
                "                inner join produce_order_item_detail on produce_order_item.id = produce_order_item_detail.`produce_order_item_id`\n" +
                "                where produce_order.code = :rootReferenceCode";
        Map<Long, BigDecimal> requestQuantityMap = sharpService.queryForKeyValue(sql2, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        if (movementType == InventoryDocument.MovementTypeEnum.OUTBOUND) {
            // 同向
            for (Map.Entry<Long, BigDecimal> entry : requestQuantityMap.entrySet()) {
                BigDecimal value = entry.getValue().subtract(ObjectUtils.defaultIfNull(histroyGoodsReceiptQuantityMap.get(entry.getKey()), BigDecimal.ZERO));
                histroyGoodsReceiptQuantityMap.put(entry.getKey(), BigDecimalUtils.lt(value, BigDecimal.ZERO) ? BigDecimal.ZERO : value);
            }
        } else {
            for (Long itemId : requestQuantityMap.keySet()) {
                if (!histroyGoodsReceiptQuantityMap.containsKey(itemId)) {
                    histroyGoodsReceiptQuantityMap.put(itemId, BigDecimal.ZERO);
                }
            }
        }

        return histroyGoodsReceiptQuantityMap;
    }

    public Map<Long, BigDecimal> saleItemOrderQuantity(String produceOrderCode) {
        return produceOrderItemDAO.selectByParamsAsMap
                (Params.builder(1).pv("produceOrderCode", produceOrderCode).build(),
                        "id, quantity", "produce_order_code = :produceOrderCode");
    }

    public Map<Long, BigDecimal> salesOpenQuantity(InventoryDocument.MovementTypeEnum movementType, String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where exists(select 1 from produce_order_item where `produce_order_code` = :rootReferenceCode AND produce_order_item.id = inv_document_item.root_reference_item_id) group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        if (movementType == InventoryDocument.MovementTypeEnum.OUTBOUND) {
            // 同向
            ProduceOrder produceOrder = produceOrderDAO.selectByCode(rootReferenceCode).orElseThrow(() -> new ResourceNotFoundException());

            for (ProduceOrder.Item item : produceOrder.getItemList()) {
                BigDecimal value = item.getQuantity().subtract(ObjectUtils.defaultIfNull(histroyGoodsReceiptQuantityMap.get(item.getId()), BigDecimal.ZERO));
                histroyGoodsReceiptQuantityMap.put(item.getId(), BigDecimalUtils.lt(value, BigDecimal.ZERO) ? BigDecimal.ZERO : value);
            }
        }

        return histroyGoodsReceiptQuantityMap;
    }

    public Map<Long, BigDecimal> salesHistoryGoodsIssueQuantity(String rootReferenceCode) {
        return salesHistoryGoodsIssueQuantity(rootReferenceCode,
                produceOrderItemDAO.selectIdsByParams(ProduceOrder.Item.builder().produceOrderCode(rootReferenceCode).build()));
    }

    public Map<Long, BigDecimal> salesHistoryGoodsIssueQuantity(String rootReferenceCode, Collection<Long> itemIdsList) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsIssueQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        for (Long itemId : itemIdsList) {
            histroyGoodsIssueQuantityMap.put(itemId, ObjectUtils.defaultIfNull(histroyGoodsIssueQuantityMap.get(itemId), BigDecimal.ZERO));
        }

        return histroyGoodsIssueQuantityMap;
    }

    /**
     * 设置状态： 领料完成
     *
     * @param rootReferenceCode
     */
//    public void setProcessingStatus(String rootReferenceCode) {
////        setStatus(rootReferenceCode, ProduceOrder.StatusEnum.PROCESSING);
//    }

    /**
     * 设置状态： 计划中
     *
//     * @param rootReferenceCode
     */
//    public void setPlanningStatus(String rootReferenceCode) {
//        setStatus(rootReferenceCode, ProduceOrder.StatusEnum.PLANNING);
//    }

//    public void setProcessingComplete(List<Long> completeIdList) {
//        setProcessingUnComplete(completeIdList, true);
//    }
//
//    public void setProcessingUnComplete(List<Long> completeIdList) {
//       setProcessingUnComplete(completeIdList, false);
//    }

//    private void setProcessingUnComplete(List<Long> completeIdList, boolean complete) {
//        if (CollectionUtils.isNotEmpty(completeIdList)) {
//            produceOrderItemDetailDAO.update("is_complete"
//                    , Params.builder(2).pv("completeIdList", completeIdList).pv("is_complete", complete).build(),
//                    "id IN (:completeIdList)");
//        }
//    }

    /**
     * 发货完成
     * @param completeIdList
     */
    public void setIssueComplete(List<Long> completeIdList) {
        setIssueUnCompleteStatus(completeIdList, true);
    }

    public void setIssueUnComplete(List<Long> completeIdList) {
        setIssueUnCompleteStatus(completeIdList, false);
    }

    public void setIssueUnCompleteStatus(List<Long> completeIdList, boolean complete) {
        if (CollectionUtils.isNotEmpty(completeIdList)) {
            produceOrderItemDAO.update("is_complete"
                    , Params.builder(2).pv("completeIdList", completeIdList).pv("is_complete", complete).build(),
                    "id IN (:completeIdList)");
        }
    }

    /**
     * 设置状态： 订单完成
     *
     * @param rootReferenceCode
     */
    public void setDoneStatus(String rootReferenceCode) {
        setStatus(rootReferenceCode, ProduceOrder.StatusEnum.DONE);
    }

    public void setStatus(String rootReferenceCode, ProduceOrder.StatusEnum status) {
        produceOrderDAO.update("status", new Object[]{status.getCode(), rootReferenceCode}, "code = ?");
    }

    private void handlePurchaseSendType(List<ProduceOrder.Item> purchaseSendItem) {
        if (CollectionUtils.isEmpty(purchaseSendItem)) {
            return;
        }

        List<PurchaseRequisition.Item> itemList = Lists.newArrayListWithExpectedSize(purchaseSendItem.size());
        for (ProduceOrder.Item item : purchaseSendItem) {
            PurchaseRequisition.Item prItem = new PurchaseRequisition.Item();
            BeanUtils.copyProperties(item, prItem);
            prItem.setReferenceType(ReferenceTypeEnum.SO);
            prItem.setReferenceId(item.getId());
            prItem.setPurchaseRequisitionId(1L);
            prItem.setPurchaseRequisitionCode("STANDARD");
            BaseEntityUtils.resetAdditionalFields(prItem);
            itemList.add(prItem);
        }

        purchaseRequisitionItemService.insertOrUpdateByReferenceIds(itemList);
    }

}


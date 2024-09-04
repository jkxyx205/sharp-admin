package com.rick.admin.module.produce.service;

import com.google.common.collect.Lists;
import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.core.model.ReferenceTypeEnum;
import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.dao.ProduceOrderItemDAO;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.purchase.entity.PurchaseRequisition;
import com.rick.admin.module.purchase.service.PurchaseRequisitionItemService;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.util.StringUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
@Slf4j
public class ProduceOrderService {

    EntityCodeDAO<ProduceOrder, Long> produceOrderDAO;

    EntityDAO<PurchaseRequisition.Item, Long> purchaseRequisitionItemDAO;

    ProduceOrderItemDAO produceOrderItemDAO;

    SharpService sharpService;

    BatchService batchService;

    PurchaseRequisitionItemService purchaseRequisitionItemService;

    MaterialService materialService;

    DictService dictService;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 新增或修改
     *
     * @param order
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(ProduceOrder order) {
        if (order.getId() == null) {
            order.setCode(CodeHelper.generateCode("SO"));
            order.setPurchaseRequisition(false);
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

        Map<Long, ProduceOrder.Item.Detail> oldParamsMap;
        Map<Long, ProduceOrder.Item> oldItemParamsMap;
        if (order.getId() != null) {
            oldParamsMap = sharpService.query("select id, material_id, material_code, batch_id, batch_code, remark, quantity from produce_order_item_detail where exists (select 1 from produce_order_item where produce_order_id = :produceOrderId and produce_order_item.id = produce_order_item_detail.produce_order_item_id)",
                    Params.builder(1).pv("produceOrderId", order.getId()).build(), ProduceOrder.Item.Detail.class).stream().collect(Collectors.toMap(item -> item.getId(), Function.identity()));
            oldItemParamsMap =  sharpService.query("select id, material_id, material_code, batch_id, batch_code, remark, quantity from produce_order_item where produce_order_id = :produceOrderId AND material_code not like 'F%'",
                    Params.builder(1).pv("produceOrderId", order.getId()).build(), ProduceOrder.Item.class).stream().collect(Collectors.toMap(item -> item.getId(), Function.identity()));
        } else {
            oldParamsMap = null;
            oldItemParamsMap = null;
        }

        batchService.saveBatch(Stream.concat(order.getItemList().stream(), order.getItemList().stream().flatMap(item -> CollectionUtils.isNotEmpty(item.getItemList()) ? item.getItemList().stream() : Stream.empty())).collect(Collectors.toSet()));
        produceOrderDAO.insertOrUpdate(order);

        if (order.getStatus() == ProduceOrder.StatusEnum.PRODUCING) {
            // 管理员 和 程文斌 触发采购申请
//            if (UserContextHolder.get().getCode().equals("admin") || UserContextHolder.get().getCode().equals("cpk")) {
                final User user = UserContextHolder.get();
                executorService.submit(() -> {
                    UserContextHolder.set(user);
                    LocalDate deliveryDate =  order.getItemList().stream().flatMap(item -> item.getScheduleList().stream()).map(ProduceOrder.Item.Schedule::getStartDate).min(LocalDate::compareTo).orElseGet(() -> order.getItemList().stream().map(ProduceOrder.Item::getDeliveryDate).min(LocalDate::compareTo).get());
                    handlePurchaseRequisition(order.getItemList(), order.getId(), order.getCode(), order.getPartnerId(), oldParamsMap, oldItemParamsMap, deliveryDate);
                });
//            }
        }

        if (order.getStatus() == ProduceOrder.StatusEnum.PRODUCED || order.getStatus() == ProduceOrder.StatusEnum.DONE) {
            // 删除未完成的采购申请
            purchaseRequisitionItemService.deleteUnCompletePurchaseRequisitionByReferenceDocumentCode(order.getCode());
        }

        log.info("{} 更新销售订单【{}】信息", UserContextHolder.get().getName(), order.getCode());
    }

    public void markItemCompleted(@NonNull Long itemId) {
        Optional<String> optional = produceOrderItemDAO.selectSingleValueById(itemId, "produce_order_code", String.class);
        if (!optional.isPresent()) {
            // 非item行， 直接返回
            return;
        }

        produceOrderItemDAO.update("is_complete", new Object[]{1, itemId}, "id = ?");
        String produceOrderCode = optional.get();
        markOrderStatusByItemStatus(produceOrderCode);
    }

    public void markOrderStatusByItemStatus(String produceOrderCode) {
        List<Boolean> completeValueList = produceOrderItemDAO.selectByParams(Params.builder(1).pv("produceOrderCode", produceOrderCode).build(),
                "is_complete", "produce_order_code = :produceOrderCode", Boolean.class);

        if (completeValueList.stream().allMatch(value -> value)) {
            produceOrderDAO.update("status", new Object[]{ProduceOrder.StatusEnum.DONE, produceOrderCode}, "code = ?");
            purchaseRequisitionItemService.deleteUnCompletePurchaseRequisitionByReferenceDocumentCode(produceOrderCode);
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
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where exists(select 1 from produce_order_item where `produce_order_code` = :rootReferenceCode AND reference_type = 'SO' AND produce_order_item.id = inv_document_item.root_reference_item_id) group by root_reference_item_id";
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

    public Map<Long, BigDecimal> salesHistoryGoodsIssueQuantity(Collection<Long> itemIds) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_item_id` IN (:itemIds) and type IN ('OUTBOUND', 'RETURN') group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsIssueQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("itemIds", itemIds).build());

        for (Long itemId : itemIds) {
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

//    /**
//     * 设置状态： 订单完成
//     *
//     * @param rootReferenceCode
//     */
//    public void setDoneStatus(String rootReferenceCode) {
//        setStatus(rootReferenceCode, ProduceOrder.StatusEnum.DONE);
//    }

    public void setStatus(String rootReferenceCode, ProduceOrder.StatusEnum status) {
        produceOrderDAO.update("status", new Object[]{status.getCode(), rootReferenceCode}, "code = ?");
        if (status == ProduceOrder.StatusEnum.PRODUCED || status == ProduceOrder.StatusEnum.DONE) {
            // 删除未完成的采购申请
            purchaseRequisitionItemService.deleteUnCompletePurchaseRequisitionByReferenceDocumentCode(rootReferenceCode);
        }
    }

    public void setStatusIfAllProduced(Long orderId, String orderCode) {
        // 如果所有的都生产完成标记订单生产完成
        List<String> statusList = sharpService.query("select produce_order_item_schedule.status from produce_order_item, produce_order_item_schedule, produce_order \n" +
                "where produce_order_item.id = produce_order_item_schedule.`produce_order_item_id` \n" +
                "AND produce_order.id = produce_order_item.`produce_order_id`\n" +
                "AND produce_order.id = :orderId", Params.builder(1).pv("orderId", orderId).build(), String.class);

        if (statusList.stream().allMatch(s -> s.equals(ProduceOrder.StatusEnum.PRODUCED.name()))) {
            produceOrderDAO.update("status", new Object[]{ProduceOrder.StatusEnum.PRODUCED.name(), orderId}, "id = ? and status <> 'DONE'");
            purchaseRequisitionItemService.deleteUnCompletePurchaseRequisitionByReferenceDocumentCode(orderCode);
        } else {
            produceOrderDAO.update("status", new Object[]{ProduceOrder.StatusEnum.PRODUCING.name(), orderId}, "id = ?");
        }
    }

    /**
     * 触发采购申请
     *
     * @param soItem
     * @param partnerId
     */
    private void handlePurchaseRequisition(List<ProduceOrder.Item> soItem, long produceOrderId, String produceOrderCode, @NotNull Long partnerId, Map<Long, ProduceOrder.Item.Detail> oldParamsMap, Map<Long, ProduceOrder.Item> oldItemParamsMap, LocalDate deliveryDate) {
        log.info("发起采购申请");
        Map<Long, ProduceOrder.Item> itemIdMap = soItem.stream().collect(Collectors.toMap(item -> item.getId(), Function.identity()));

        List<PurchaseRequisition.Item> requisitionItemList = Lists.newArrayList();
        // item 线 + 采购直发
        for (ProduceOrder.Item item : soItem) {
            if (item.getItemCategory() == ProduceOrder.ItemCategoryEnum.PURCHASE_SEND
                    || item.getMaterialId() == 729584784212238336L
                    || item.getMaterialId() == 741996205273632769L
                    || item.getMaterialId() == 731499486144483329L) {
                // 线 + 采购直发 按照订单实际数量采购
                PurchaseRequisition.Item prItem = new PurchaseRequisition.Item();
                BeanUtils.copyProperties(item, prItem);
                prItem.setReferenceType(ReferenceTypeEnum.SO);
                prItem.setReferenceDocumentId(produceOrderId);
                prItem.setReferenceDocumentCode(produceOrderCode);
                prItem.setReferenceId(item.getId());
                prItem.setPurchaseRequisitionId(1L);
                prItem.setPurchaseRequisitionCode("STANDARD");
                prItem.setPurchaseSend(item.getItemCategory() == ProduceOrder.ItemCategoryEnum.PURCHASE_SEND);
                prItem.setComplete(false);
                BaseEntityUtils.resetAdditionalFields(prItem);
                requisitionItemList.add(prItem);
            }
        }

        // detail 线
        List<ProduceOrder.Item.Detail> lineDetailList = soItem.stream().flatMap(item -> item.getItemList().stream()).filter(item -> Arrays.asList(729584784212238336L, 741996205273632769L, 731499486144483329L).contains(item.getMaterialId()))
                .collect(Collectors.toList());
        Set<String> purchaseIgnoreSet = lineDetailList.stream().map(item -> item.getMaterialId() + Objects.toString(item.getBatchId(), "")).collect(Collectors.toSet());
        purchaseIgnoreSet.add("764459407009763328");

        for (ProduceOrder.Item.Detail detail : lineDetailList) {
            ProduceOrder.Item item = itemIdMap.get(detail.getProduceOrderItemId());
            PurchaseRequisition.Item prItem = new PurchaseRequisition.Item();
            BeanUtils.copyProperties(detail, prItem);
            prItem.setDeliveryDate(LocalDate.now().plusDays(10)); //
            prItem.setReferenceType(ReferenceTypeEnum.SO);
            prItem.setReferenceDocumentId(produceOrderId);
            prItem.setReferenceDocumentCode(produceOrderCode);
            prItem.setReferenceId(detail.getId());
            prItem.setPurchaseRequisitionId(1L);
            prItem.setPurchaseRequisitionCode("STANDARD");
            prItem.setPurchaseSend(prItem.getRemark().contains("发"));
            prItem.setComplete(false);
            prItem.setQuantity(prItem.getQuantity().multiply(item.getQuantity()));
            BaseEntityUtils.resetAdditionalFields(prItem);
            if (prItem.getMaterialCode().equals("R00548") || prItem.getMaterialCode().equals("R00848") || prItem.getMaterialCode().equals("R00904")) {
                // 控制线带上客户信息
                prItem.setRemark(dictService.getDictByTypeAndName("core_partner_customer", String.valueOf(partnerId)).get().getLabel() +  StringUtils.appendValue(prItem.getRemark()));
            }
            requisitionItemList.add(prItem);
        }

        // 查询库存的采购（item + detail）
        List<PurchaseRequisition.Item> stockRequisitionItemList = purchaseRequisitionForProduce(produceOrderId, purchaseIgnoreSet);
        if (CollectionUtils.isNotEmpty(stockRequisitionItemList)) {
            materialService.fillMaterialDescription(stockRequisitionItemList);

            Map<String, BigDecimal> orderQuantityMap = new HashMap<>();
            for (ProduceOrder.Item item : soItem) {
                String itemMaterialKey = item.getMaterialId() + Objects.toString(item.getBatchId(), "");
                if (orderQuantityMap.get(itemMaterialKey) == null) {
                    orderQuantityMap.put(itemMaterialKey, item.getQuantity());
                } else {
                    orderQuantityMap.put(itemMaterialKey, item.getQuantity().add(orderQuantityMap.get(itemMaterialKey)));
                }

                for (ProduceOrder.Item.Detail detail : item.getItemList()) {
                    if (purchaseIgnoreSet.contains(detail.getId())) {
                        continue;
                    }
                    String detailMaterialKey = detail.getMaterialId() + Objects.toString(detail.getBatchId(), "");
                    BigDecimal quantity = orderQuantityMap.get(detailMaterialKey);
                    if (Objects.isNull(quantity)) {
                        quantity = BigDecimal.ZERO;

                    }
                    orderQuantityMap.put(detailMaterialKey, quantity.add(item.getQuantity().multiply(detail.getQuantity())));
                }
            }

            for (PurchaseRequisition.Item prItem : stockRequisitionItemList) {
                prItem.setReferenceType(ReferenceTypeEnum.SO);
                prItem.setScheduleId(produceOrderId);
                prItem.setReferenceDocumentId(produceOrderId);
                prItem.setReferenceDocumentCode(produceOrderCode);
                prItem.setReferenceId(prItem.getMaterialId());
                prItem.setBatchCode(prItem.getBatchCode());
                prItem.setBatchId(prItem.getBatchId());
                prItem.setPurchaseRequisitionId(1L);
                prItem.setPurchaseRequisitionCode("STANDARD");
                prItem.setPurchaseSend(false);
                prItem.setMaterialCode(prItem.getMaterialDescription().getCode());
                prItem.setUnit(prItem.getMaterialDescription().getUnit());
                prItem.setComplete(false);
                prItem.setDeliveryDate(deliveryDate);
                // 物料合并采购， remark 不需要
//                prItem.setRemark(Objects.toString(materialIdRemark.get(prItem.getMaterialId() + Objects.toString(prItem.getBatchCode(), "")), ""));

                // 原单数量采购， 不依据计算的结果
                prItem.setQuantity(orderQuantityMap.get(prItem.getMaterialId() + Objects.toString(prItem.getBatchId(), "")).setScale(0, RoundingMode.UP));
                // 设置 数量 Tolerance 10%
//                prItem.setQuantity(prItem.getQuantity().multiply(BigDecimal.valueOf(1.1)).setScale(0, RoundingMode.UP));
                // 取整10
//                BigDecimal quantity = prItem.getQuantity().intValue() % 10 == 0 ? prItem.getQuantity() : BigDecimal.valueOf((prItem.getQuantity().intValue() / 10 + 1) * 10);
//                prItem.setQuantity(quantity);

                requisitionItemList.add(prItem);
            }
        }

        if (CollectionUtils.isEmpty(requisitionItemList)) {
            return;
        }

        Boolean purchaseRequisition = produceOrderDAO.selectSingleValueById(produceOrderId, "is_purchase_requisition", Boolean.class).get();

        if (purchaseRequisition) {
            if (purchaseRequisition && (MapUtils.isNotEmpty(oldItemParamsMap) || MapUtils.isNotEmpty(oldParamsMap))) {
                // 只采购修改的参数的物料
                List<ProduceOrder.Item.Detail> newDetailList = soItem.stream().flatMap(item -> item.getItemList().stream()).collect(Collectors.toList());
                boolean paramsChanged = false;
                Set<Long> itemIds = new HashSet<>();
                // 处理item
                for (ProduceOrder.Item item : soItem) {
                    if (!item.getMaterialCode().startsWith("F")) {
                        if (item.getId() != null) {
                            itemIds.add(item.getId());
                        }
                        ProduceOrder.Item oldItem = oldItemParamsMap.get(item.getId());

                        if (oldItem == null || !(Objects.equals(item.getMaterialId(), oldItem.getMaterialId()) && Objects.equals(item.getBatchId(), oldItem.getBatchId())) ||
                                !Objects.equals(Objects.toString(oldItem.getRemark(), ""), Objects.toString(item.getRemark(), "")) || !BigDecimalUtils.eq(item.getQuantity(), oldItem.getQuantity())) {
                            paramsChanged = true;
                            break;
                        }
                    }
                }

                if (!paramsChanged) {
                    for (ProduceOrder.Item oldItem : oldItemParamsMap.values()) {
                        if (!itemIds.contains(oldItem.getId())) {
                            paramsChanged = true;
                            break;
                        }
                    }
                }

                // 处理detail
                for (ProduceOrder.Item.Detail detail : newDetailList) {
                    ProduceOrder.Item.Detail oldDetail = oldParamsMap.get(detail.getId());
                    if (oldDetail == null || !(Objects.equals(detail.getMaterialId(), oldDetail.getMaterialId()) && Objects.equals(detail.getBatchId(), oldDetail.getBatchId())) ||
                            !Objects.equals(Objects.toString(oldDetail.getRemark(), ""), Objects.toString(detail.getRemark(), "")) ||
                        !BigDecimalUtils.eq(detail.getQuantity(), oldDetail.getQuantity())) {
                        paramsChanged = true;
                        break;
                    }
                }

                if (!paramsChanged) {
                    return;
                }
            }

            // 如果有过采购记录， 需要根据采购申请进行比对，获取差异
            List<PurchaseRequisition.Item> comparedRequisitionItemList = Lists.newArrayList();

            List<PurchaseRequisition.Item> purchaseRequisitionList = purchaseRequisitionItemDAO.selectByParams(Params.builder(1).pv("produceOrderId", produceOrderId).build(), "reference_document_id = :produceOrderId and is_deleted = 0");
            List<PurchaseRequisition.Item> purchaseRequisitionListClone = purchaseRequisitionList.stream().map(SerializationUtils::clone).collect(Collectors.toList());

            Map<String, PurchaseRequisition.Item> keyPurchaseRequisitionItemMap =
                    purchaseRequisitionListClone.stream().collect(Collectors.toMap(item -> purchaseMaterialKey(item.getReferenceId(), item.getMaterialId(), item.getBatchId(), item.getRemark()), Function.identity(), (item1, item2) -> { item1.setQuantity(item1.getQuantity().add(item2.getQuantity())); return item1;}));
            Map<String, PurchaseRequisition.Item> requisitionItemListMap = requisitionItemList.stream().collect(Collectors.toMap(item -> purchaseMaterialKey(item.getReferenceId(), item.getMaterialId(), item.getBatchId(), item.getRemark()), Function.identity()));
            Set<Long> deletedPurchaseRequisitionItemIds = new HashSet<>();

            for (PurchaseRequisition.Item item : requisitionItemList) {
                PurchaseRequisition.Item oldPurchaseRequisitionItem = keyPurchaseRequisitionItemMap.get(purchaseMaterialKey(item.getReferenceId(), item.getMaterialId(), item.getBatchId(), item.getRemark()));
                if (oldPurchaseRequisitionItem == null) {
                    // 没有采购记录， 直接插入
                    comparedRequisitionItemList.add(item);
                } else if (BigDecimalUtils.gt(item.getQuantity(), oldPurchaseRequisitionItem.getQuantity())) {
                    // 采购数量变大了
                    boolean hasPurchased = false;
                    BigDecimal hasPurchasedQuantity = BigDecimal.ZERO;

                    for (PurchaseRequisition.Item item1 : purchaseRequisitionList) {
                        // TODO 行项目物料 不应该考虑 itemId
                        if (purchaseMaterialKey(item1.getReferenceId(), item1.getMaterialId(), item1.getBatchId(), item1.getRemark()).equals(purchaseMaterialKey(item.getReferenceId(), item.getMaterialId(), item.getBatchId(), item.getRemark()))) {
                            if (item1.getComplete()) {
                                hasPurchased = true;
                                hasPurchasedQuantity = hasPurchasedQuantity.add(item1.getQuantity());
                            } else {
                                deletedPurchaseRequisitionItemIds.add(item1.getId());
                            }
                        }
                    }

                    if (hasPurchased) {
                        // 已经采购过了
                        item.setQuantity(item.getQuantity().subtract(hasPurchasedQuantity));
                        comparedRequisitionItemList.add(item);
                    } else {
                        // 尚未采购
                        comparedRequisitionItemList.add(item);
                        deletedPurchaseRequisitionItemIds.add(oldPurchaseRequisitionItem.getId());
                    }
                }
            }

            // 删除不需要的采购申请
            for (PurchaseRequisition.Item item : purchaseRequisitionList) {
                if (!requisitionItemListMap.keySet().contains(purchaseMaterialKey(item.getReferenceId(), item.getMaterialId(), item.getBatchId(), item.getRemark()))) {
                    deletedPurchaseRequisitionItemIds.add(item.getId());
                }
            }

            if (CollectionUtils.isNotEmpty(deletedPurchaseRequisitionItemIds)) {
                purchaseRequisitionItemDAO.delete(Params.builder(1).pv("ids", deletedPurchaseRequisitionItemIds).build(), "id IN (:ids)");
            }

            requisitionItemList = comparedRequisitionItemList;
        }

        materialService.fillMaterialDescription(requisitionItemList);
        // 对特定物料发起采购申请
//        requisitionItemList = requisitionItemList.stream().filter(item -> SpecialMaterialConstant.isSpecialSpecialMaterialCategory(item.getMaterialDescription().getCategoryId())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(requisitionItemList)) {
            return;
        }

        produceOrderDAO.update("is_purchase_requisition", new Object[]{1, produceOrderId}, "id = ?");
        purchaseRequisitionItemService.insertOrUpdateByReferenceIds(requisitionItemList);
    }

    private List<PurchaseRequisition.Item> purchaseRequisitionForProduce(long produceOrderId, Set<String> materialIdBatchIdString) {
        String sql = "select material_id materialId, batch_id batchId, batch_code batchCode,  -1 * sum(quantity) quantity, 0 purchaseSend FROM (\n" +
                "                                                              select\n" +
                "                                                                  receiving.material_id,\n" +
                "                                                                  receiving.batch_id,\n" +
                "                                                                  receiving.batch_code,\n" +
                "                                                                  (receiving.quantity - ifnull(received.received_quantity, 0)) quantity\n" +
                "                                                              from (select pur_purchase_order_item.*\n" +
                "                                                                    from `pur_purchase_order`,\n" +
                "                                                                         pur_purchase_order_item\n" +
                "                                                                    WHERE pur_purchase_order.status = 'PLANNING'\n" +
                "                                                                      and pur_purchase_order.is_deleted = 0\n" +
                "                                                                      AND pur_purchase_order.id = pur_purchase_order_item.`purchase_order_id`\n" +
                "                                                                      AND pur_purchase_order_item.`is_complete` = 0 " +
                "                                                                      AND pur_purchase_order_item.is_purchase_send = 0) receiving\n" +
                "                                                                       left join\n" +
                "                                                                   (select root_reference_item_id,\n" +
                "                                                                           ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) received_quantity\n" +
                "                                                                    from inv_document_item\n" +
                "                                                                    where Exists(select 1\n" +
                "                                                                                 from `pur_purchase_order`,\n" +
                "                                                                                      pur_purchase_order_item\n" +
                "                                                                                 where pur_purchase_order.status = 'PLANNING'\n" +
                "                                                                                   and pur_purchase_order.is_deleted = 0\n" +
                "                                                                                   AND pur_purchase_order.id = pur_purchase_order_item.`purchase_order_id`\n" +
                "                                                                                   AND pur_purchase_order_item.`is_complete` = 0\n" +
                "                                                                                   AND pur_purchase_order_item.`is_purchase_send` = 0\n" +
                "                                                                                   AND pur_purchase_order_item.id = root_reference_item_id)\n" +
                "                                                                    group by root_reference_item_id) received\n" +
                "                                                                   on receiving.id = received.root_reference_item_id\n" +
                "                                                              union all\n" +
                "                                                              select material_id, batch_id, batch_code, quantity from inv_stock where plant_id = 719893335619162112 " +
                " AND material_id NOT IN (729584784212238336, 741996205273632769, 731499486144483329, 764459407009763328)\n" +
                "                                                              union all\n" +
                "                                                              select produce_order_item.material_id, produce_order_item.batch_id, produce_order_item.batch_code, -1 * produce_order_item.quantity from produce_order_item, mm_material where item_category='PRODUCT' AND produce_order_item.is_complete = 0 and mm_material.id = produce_order_item.material_id AND mm_material.material_type = 'ROH'\n" +
                "                                                              union all" +
                "                                                              select material_id, batch_id, batch_code, inv_document_item.quantity from inv_document_item, produce_order_item_schedule, produce_order where type = 'MOVING_TO_PRODUCE' AND plant_id = 719893335619162112 and reference_type = 'PP' and inv_document_item.root_reference_code = produce_order_item_schedule.code and produce_order_item_schedule.`produce_order_id` = produce_order.id and produce_order.status = 'PRODUCING'" +
                "                                                              union all\n" +
                "                                                              select material_id, batch_id, batch_code, quantity from pur_purchase_requisition_item where is_complete = 0 and is_deleted = 0 and reference_document_id <> :produceOrderId\n" +
                "                                                              union all\n" +
                "                                                              select produce_order_item_detail.material_id, produce_order_item_detail.batch_id, produce_order_item_detail.batch_code,  (-1 * schedule.quantity * (CASE\n" +
                "WHEN produce_order_item_detail.component_detail_id = 725451860537794560 THEN 3 * produce_order_item_detail.quantity\n" +
                "WHEN produce_order_item_detail.component_detail_id = 725451860537794561 THEN 3 * produce_order_item_detail.quantity\n" +
                "ELSE produce_order_item_detail.quantity\n" +
                "END)) quantity  from produce_order_item, produce_order, produce_order_item_detail, produce_order_item_schedule schedule\n" +
                "                                                              where produce_order.id = produce_order_item.produce_order_id AND schedule.produce_order_item_id = produce_order_item.id AND schedule.status = 'PRODUCING' AND produce_order_item_detail.produce_order_item_id = produce_order_item.id\n" +
                "                                                                AND produce_order.`status` = 'PRODUCING' AND produce_order_item.item_category = 'PRODUCT'\n" +
                "                                                          ) stock\n" +
                "                                                     where exists(\n" +
                "                                                                   select 1 from produce_order_item, produce_order, (select material_id, batch_id, produce_order_item_id from produce_order_item_detail\n" +
                "union all\n" +
                " select produce_order_item.material_id, produce_order_item.batch_id, produce_order_item.id produce_order_item_id from produce_order_item, mm_material where produce_order_id = :produceOrderId and item_category='PRODUCT' and produce_order_item.is_complete = 0 and mm_material.id = produce_order_item.material_id AND mm_material.material_type = 'ROH'\n" +
                ") produce_order_item_detail\n" +
                "                                                                   where produce_order.id = produce_order_item.produce_order_id AND produce_order_item_detail.produce_order_item_id = produce_order_item.id\n" +
                "                                                                      AND produce_order.`status` = 'PRODUCING' AND produce_order.id = :produceOrderId\n" +
                "                                                         AND produce_order_item.item_category = 'PRODUCT'                                                         AND produce_order_item_detail.material_id = stock.material_id AND (produce_order_item_detail.batch_id = stock.batch_id or (produce_order_item_detail.batch_id is null AND stock.batch_id is null ))" +
                "                     AND concat(produce_order_item_detail.material_id, ifnull(produce_order_item_detail.batch_id, '')) NOT IN (:materialIdBatchIdString) AND produce_order_item.material_id NOT IN(729584784212238336, 741996205273632769, 731499486144483329, 764459407009763328)\n" +
                "                                                               )\n" +
                "group by material_id, batch_id having sum(quantity) < 0";

        return sharpService.query(sql, Params.builder(2).pv("produceOrderId", produceOrderId).pv("materialIdBatchIdString", materialIdBatchIdString).build(), PurchaseRequisition.Item.class);
    }

    /**
     *
     * @param id itemId detailId null
     * @param materialId
     * @param batchId
     * @param remark
     * @return
     */
    private String purchaseMaterialKey(Long id, Long materialId, Long batchId, String remark) {
        return Objects.toString(id, "") + materialId + Objects.toString(batchId, "") + Objects.toString(remark, "");
    }
}


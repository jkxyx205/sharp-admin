package com.rick.admin.module.inventory.controller;

import com.google.common.collect.Lists;
import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.HandlerHelper;
import com.rick.admin.module.inventory.service.HandlerManager;
import com.rick.admin.module.inventory.service.InventoryDocumentService;
import com.rick.admin.module.material.dao.ClassificationDAO;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.dao.ProduceOrderDAO;
import com.rick.admin.module.produce.dao.ProduceOrderItemDetailDAO;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.ProduceOrderService;
import com.rick.admin.module.produce.service.ProduceScheduleService;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.StringUtils;
import com.rick.db.dto.SimpleEntity;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/6 16:01
 */
@Controller
@RequestMapping("inventory")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class InventoryController {

    final HandlerManager handlerManager;

    final InventoryDocumentDAO inventoryDocumentDAO;

    final MaterialDAO materialDAO;

    final DictService dictService;

    final InventoryDocumentService inventoryDocumentService;

    final PurchaseOrderDAO purchaseOrderDAO;

    final PurchaseOrderService purchaseOrderService;

    final SharpService sharpService;

    final ProduceOrderService produceOrderService;

    final ProduceOrderDAO produceOrderDAO;

    final MaterialService materialService;

    final BatchService batchService;

    final ProduceOrderItemDetailDAO produceOrderItemDetailDAO;

    final ClassificationDAO classificationDAO;

    final ProduceScheduleService produceScheduleService;

    @GetMapping("move")
    public String gotoInventoryPage() {
        return "modules/inventory/index";
    }

    @PostMapping
    @ResponseBody
    public Result<String> post(@RequestBody InventoryDocument inventoryDocument) {
        handlerManager.handle(inventoryDocument);
        return ResultUtils.success(inventoryDocument.getCode());
    }

    @GetMapping("documents")
    @ResponseBody
    public InventoryDocument getInventoryDocument(InventoryDocument.TypeEnum type, InventoryDocument.ReferenceTypeEnum referenceType, String referenceCode) {
        InventoryDocument inventoryDocument;
        if (referenceType == InventoryDocument.ReferenceTypeEnum.MATERIAL_DOCUMENT) {
            Optional<InventoryDocument> optional = inventoryDocumentDAO.selectByCode(referenceCode);

            if (!optional.isPresent()) {
                throw new BizException(ExceptionCodeEnum.MATERIAL_DOCUMENT_NOT_FOUND_ERROR, new Object[]{referenceCode});
            }

            inventoryDocument = optional.get();

            if (inventoryDocument.getType() == InventoryDocument.TypeEnum.COUNT && type != InventoryDocument.TypeEnum.DISPLAY) {
                throw new BizException(ExceptionCodeEnum.COUNT_MATERIAL_DOCUMENT_REF_ERROR);
            }

            Map<Long, BigDecimal> itemOpenQuantityMap = null;

            if (type == InventoryDocument.TypeEnum.RETURN) {
                InventoryDocument.MovementTypeEnum oppositeMovementType = HandlerHelper.oppositeMovementType(inventoryDocument.getItemList().get(0).getMovementType());
                if (inventoryDocument.getReferenceType() == InventoryDocument.ReferenceTypeEnum.PO) {
                    itemOpenQuantityMap = purchaseOrderService.openQuantity(oppositeMovementType, inventoryDocument.getRootReferenceCode());
                } else if (inventoryDocument.getReferenceType() == InventoryDocument.ReferenceTypeEnum.PP) {
                    itemOpenQuantityMap = produceOrderService.openQuantity(oppositeMovementType, inventoryDocument.getRootReferenceCode());
                } else if (inventoryDocument.getReferenceType() == InventoryDocument.ReferenceTypeEnum.SO) {
                    itemOpenQuantityMap = produceOrderService.salesOpenQuantity(oppositeMovementType, inventoryDocument.getRootReferenceCode());
                } else {
                    itemOpenQuantityMap = inventoryDocumentService.openQuantity(HandlerHelper.oppositeMovementType(inventoryDocument.getItemList().get(0).getMovementType()),
                            inventoryDocument.getCode(), inventoryDocument.getRootReferenceCode());
                }
            }

            for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
                item.setReferenceItemId(item.getId());
                if (type == InventoryDocument.TypeEnum.RETURN) {
                    item.setQuantity(itemOpenQuantityMap.get(item.getRootReferenceItemId()));
                }
            }

            materialService.fillMaterialDescription(inventoryDocument.getItemList());
        } else if (referenceType == InventoryDocument.ReferenceTypeEnum.PO) {
            inventoryDocument = getDocumentFromPurchaseOrder(type == InventoryDocument.TypeEnum.RETURN ? InventoryDocument.MovementTypeEnum.OUTBOUND : InventoryDocument.MovementTypeEnum.INBOUND, type, referenceType, referenceCode);
        } else if (referenceType == InventoryDocument.ReferenceTypeEnum.PP) {
            inventoryDocument = getDocumentFromProduceSchedule(type == InventoryDocument.TypeEnum.RETURN_FROM_PRODUCE ? InventoryDocument.MovementTypeEnum.INBOUND : InventoryDocument.MovementTypeEnum.OUTBOUND, type, referenceType, referenceCode);
        }  else if (referenceType == InventoryDocument.ReferenceTypeEnum.SO) {
            inventoryDocument =  getDocumentFromSalesOrder(type == InventoryDocument.TypeEnum.RETURN ? InventoryDocument.MovementTypeEnum.INBOUND : InventoryDocument.MovementTypeEnum.OUTBOUND, type, referenceType, referenceCode);
        } else {
            throw new BizException("无法获取 " + referenceType.name());
        }

        materialService.fillMaterialDescription(inventoryDocument.getItemList());
        batchService.fillCharacteristicValue(inventoryDocument.getItemList());
        return inventoryDocument;
    }

    private InventoryDocument getDocumentFromPurchaseOrder(InventoryDocument.MovementTypeEnum movementType, InventoryDocument.TypeEnum type, InventoryDocument.ReferenceTypeEnum referenceType, String referenceCode) {
        PurchaseOrder purchaseOrder;
        if (referenceCode.startsWith("PO")) {
            // 完整 code 输入
            purchaseOrder = purchaseOrderDAO.selectByCode(referenceCode)
                    .orElseThrow(() -> new BizException(ExceptionCodeEnum.PO_DOCUMENT_NOT_FOUND_ERROR, new Object[]{referenceCode}));
        } else {
            purchaseOrder = purchaseOrderDAO.findActivePurchaseOrderByKeyCode(referenceCode)
                    .orElseThrow(() -> new BizException("没有找到订单号，请输入更多的编号或者完整订单号！"));
        }

        InventoryDocument inventoryDocument = InventoryDocument.builder()
                .type(type)
                .referenceType(referenceType)
                .referenceCode(purchaseOrder.getCode())
                .rootReferenceCode(purchaseOrder.getCode())
                .plantId(purchaseOrder.getPlantId())
                .operatorId(UserContextHolder.get().getId())
                .documentDate(LocalDate.now())
                .attachmentList(Collections.emptyList())
                .build();

        inventoryDocument.setItemList(Lists.newArrayListWithExpectedSize(purchaseOrder.getItemList().size()));

        for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
            if (item.getPurchaseSend()) {
                continue;
            }

            inventoryDocument.getItemList()
                    .add(InventoryDocument.Item.builder()
                            .type(type)
                            .referenceType(referenceType)
                            .referenceCode(purchaseOrder.getCode())
                            .referenceItemId(item.getId())
                            .rootReferenceCode(purchaseOrder.getCode())
                            .rootReferenceItemId(item.getId())
                            .movementType(InventoryDocument.MovementTypeEnum.INBOUND)
                            .plantId(purchaseOrder.getPlantId())
                            .batchId(item.getBatchId())
                            .batchCode(item.getBatchCode())
                            .classificationList(item.getClassificationList())
                            .materialId(item.getMaterialId())
                            .materialCode(item.getMaterialCode())
                            .quantity(item.getQuantity())
                            .unit(item.getUnit())
                            .remark(dictService.getDictByTypeAndName("core_partner_vendor", String.valueOf(purchaseOrder.getPartnerId())).get().getLabel() + StringUtils.appendValue(item.getRemark()))
                            .build());
        }

        Map<Long, BigDecimal> itemOpenQuantityMap = purchaseOrderService.openQuantity(movementType, purchaseOrder.getCode());
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setQuantity(ObjectUtils.defaultIfNull(itemOpenQuantityMap.get(item.getRootReferenceItemId()), BigDecimal.ZERO));
        }

        return inventoryDocument;
    }

    private InventoryDocument getDocumentFromProduceSchedule(InventoryDocument.MovementTypeEnum movementType, InventoryDocument.TypeEnum type, InventoryDocument.ReferenceTypeEnum referenceType, String referenceCode) {
        if (!referenceCode.startsWith("PP")) {
            referenceCode = produceScheduleService.findActiveProduceScheduleByKeyCode(referenceCode);
        }

        String sql = "select produce_order_item_detail.`material_id`,\n" +
                "       mm_material.code material_code,       produce_order_item_detail.batch_id,       produce_order_item_detail.batch_code,       produce_order_item_detail.`id`                               referenceItemId,\n" +
                "       produce_order_item_detail.`id`                               rootReferenceItemId,\n" +
                "       produce_order_item_detail.quantity * produce_order_item_schedule.quantity quantity,\n" +
                "       mm_material.base_unit                                          unit\n" +
                "from produce_order_item_schedule\n" +
                "         inner join produce_order_item on produce_order_item.id = produce_order_item_schedule.produce_order_item_id\n" +
                "         inner join produce_order_item_detail on produce_order_item.id = produce_order_item_detail.`produce_order_item_id`\n" +
                "         inner join mm_material on mm_material.id = produce_order_item_detail.`material_id`\n" +
                "where produce_order_item_schedule.code = :referenceCode";

        List<InventoryDocument.Item> itemList = sharpService.query(sql, Params.builder(1).pv("referenceCode", referenceCode).build(), InventoryDocument.Item.class);
        if (CollectionUtils.isEmpty(itemList)) {
            throw new BizException(ExceptionCodeEnum.PP_DOCUMENT_NOT_FOUND_ERROR, new Object[]{referenceCode});
        }

        InventoryDocument inventoryDocument = InventoryDocument.builder()
                .type(type)
                .referenceType(referenceType)
                .referenceCode(referenceCode)
                .rootReferenceCode(referenceCode)
                .operatorId(UserContextHolder.get().getId())
                .documentDate(LocalDate.now())
                .attachmentList(Collections.emptyList())
                .build();

        inventoryDocument.setItemList(itemList);

        Map<Long, BigDecimal> itemOpenQuantityMap = produceScheduleService.openQuantity(movementType, referenceCode);

//        Map<Long, ProduceOrder.Item.Detail> idDetailMap = produceOrderItemDetailDAO.selectByIdsAsMap(itemList.stream().map(InventoryDocument.Item::getReferenceItemId).collect(Collectors.toSet()));
//        Map<Long, List<Classification>> materialIdClassificationMap = classificationDAO.findMaterialClassificationByMaterialIds(itemList.stream().map(InventoryDocument.Item::getMaterialId).collect(Collectors.toSet()));
        for (InventoryDocument.Item item : itemList) {
            item.setType(type);
            item.setReferenceType(referenceType);
            item.setReferenceCode(referenceCode);
            item.setRootReferenceCode(referenceCode);
            item.setMovementType(movementType);
            item.setQuantity(ObjectUtils.defaultIfNull(itemOpenQuantityMap.get(item.getRootReferenceItemId()), BigDecimal.ZERO));
//            item.setClassificationList(idDetailMap.get(item.getReferenceItemId()).getClassificationList());
//            item.setClassificationList(ObjectUtils.defaultIfNull(materialIdClassificationMap.get(item.getMaterialId()), Collections.emptyList()));
        }

        batchService.handleClassificationAndFillCharacteristicValue(itemList);
        inventoryDocument.setReferenceCode(referenceCode);
        return inventoryDocument;
    }

    private InventoryDocument getDocumentFromSalesOrder(InventoryDocument.MovementTypeEnum movementType, InventoryDocument.TypeEnum type, InventoryDocument.ReferenceTypeEnum referenceType, String referenceCode2) {
        String referenceCode = referenceCode2;
        if (!referenceCode2.startsWith("SO")) {
            referenceCode = produceOrderDAO.findActiveProduceOrderByKeyCode(referenceCode2);
        }

        com.rick.admin.module.produce.entity.ProduceOrder produceOrder = produceOrderDAO.selectByCode(referenceCode)
                .orElseThrow(() -> new BizException(ExceptionCodeEnum.PP_DOCUMENT_NOT_FOUND_ERROR, new Object[]{referenceCode2}));
        InventoryDocument inventoryDocument = InventoryDocument.builder()
                .type(type)
                .referenceType(referenceType)
                .referenceCode(referenceCode)
                .rootReferenceCode(referenceCode)
                .operatorId(UserContextHolder.get().getId())
                .documentDate(LocalDate.now())
                .attachmentList(Collections.emptyList())
                .build();

        inventoryDocument.setItemList(Lists.newArrayListWithExpectedSize(produceOrder.getItemList().size()));

        for (ProduceOrder.Item item : produceOrder.getItemList()) {
            if (item.getItemCategory() == ProduceOrder.ItemCategoryEnum.PURCHASE_SEND) {
                continue;
            }

            inventoryDocument.getItemList()
                    .add(InventoryDocument.Item.builder()
                            .type(type)
                            .referenceType(referenceType)
                            .referenceCode(referenceCode)
                            .referenceItemId(item.getId())
                            .rootReferenceCode(referenceCode)
                            .rootReferenceItemId(item.getId())
                            .movementType(InventoryDocument.MovementTypeEnum.OUTBOUND)
                            .batchId(item.getBatchId())
                            .batchCode(item.getBatchCode())
                            .classificationList(item.getClassificationList())
                            .materialId(item.getMaterialId())
                            .materialCode(item.getMaterialCode())
                            .quantity(item.getQuantity())
                            .unit(item.getUnit())
                            .build());
        }

        Map<Long, BigDecimal> itemOpenQuantityMap = produceOrderService.salesOpenQuantity(movementType, referenceCode);
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setQuantity(ObjectUtils.defaultIfNull(itemOpenQuantityMap.get(item.getRootReferenceItemId()), BigDecimal.ZERO));
        }

        inventoryDocument.setCode(referenceCode);
        return inventoryDocument;
    }


    @GetMapping("count")
    public String gotoInventoryCountPage() {
        return "modules/inventory/count";
    }

    @PostMapping("count")
    @ResponseBody
    public Result<String> gotoInventoryCount(@RequestBody InventoryDocument inventoryDocument) {
        inventoryDocument.setType(InventoryDocument.TypeEnum.COUNT);
        inventoryDocument.setReferenceType(InventoryDocument.ReferenceTypeEnum.OTHER);
        handlerManager.handle(inventoryDocument);
        return ResultUtils.success(inventoryDocument.getCode());
    }

    /**
     * 更新备注和附件
     * @param code
     * @return
     */
    @PutMapping("{code}")
    @ResponseBody
    public Result update(@PathVariable String code, @RequestBody InventoryDocument inventoryDocument) {
        inventoryDocumentService.updateInventoryDocumentByCode(code, inventoryDocument.getAttachmentList(), inventoryDocument.getRemark(),
                inventoryDocument.getItemList().stream().collect(Collectors.toMap(SimpleEntity::getId, InventoryDocument.Item::getRemark)));
        return ResultUtils.success();
    }

}

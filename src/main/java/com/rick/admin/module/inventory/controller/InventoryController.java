package com.rick.admin.module.inventory.controller;

import com.google.common.collect.Lists;
import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.HandlerHelper;
import com.rick.admin.module.inventory.service.HandlerManager;
import com.rick.admin.module.inventory.service.InventoryDocumentService;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.service.ProduceOrderService;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    final MaterialService materialService;

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
        if (referenceType == InventoryDocument.ReferenceTypeEnum.MATERIAL_DOCUMENT) {
            Optional<InventoryDocument> optional = inventoryDocumentDAO.selectByCode(referenceCode);

            if (!optional.isPresent()) {
                throw new BizException(ExceptionCodeEnum.MATERIAL_DOCUMENT_NOT_FOUND_ERROR, new Object[]{referenceCode});
            }

            InventoryDocument inventoryDocument = optional.get();

            if (inventoryDocument.getType() == InventoryDocument.TypeEnum.COUNT && type != InventoryDocument.TypeEnum.DISPLAY) {
                throw new BizException(ExceptionCodeEnum.COUNT_MATERIAL_DOCUMENT_REF_ERROR);
            }

            Map<Long, BigDecimal> itemOpenQuantityMap = null;

            if (inventoryDocument.getReferenceType() == InventoryDocument.ReferenceTypeEnum.PO && type == InventoryDocument.TypeEnum.RETURN) {
//                return getDocument(type == InventoryDocument.TypeEnum.RETURN ? InventoryDocument.MovementTypeEnum.OUTBOUND : InventoryDocument.MovementTypeEnum.INBOUND, type, referenceType, inventoryDocument.getRootReferenceCode());
                itemOpenQuantityMap = purchaseOrderService.openQuantity(type == InventoryDocument.TypeEnum.RETURN ? InventoryDocument.MovementTypeEnum.OUTBOUND : InventoryDocument.MovementTypeEnum.INBOUND, inventoryDocument.getRootReferenceCode());
            } else if (inventoryDocument.getReferenceType() == InventoryDocument.ReferenceTypeEnum.PDO && type == InventoryDocument.TypeEnum.RETURN) {
                itemOpenQuantityMap = produceOrderService.openQuantity(type == InventoryDocument.TypeEnum.RETURN ? InventoryDocument.MovementTypeEnum.INBOUND : InventoryDocument.MovementTypeEnum.OUTBOUND, inventoryDocument.getRootReferenceCode());
            } else if (type == InventoryDocument.TypeEnum.RETURN) {
                itemOpenQuantityMap = inventoryDocumentService.openQuantity(HandlerHelper.oppositeMovementType(inventoryDocument.getItemList().get(0).getMovementType()),
                        inventoryDocument.getRootReferenceCode());
            }

            for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
                item.setReferenceItemId(item.getId());
                if (type == InventoryDocument.TypeEnum.RETURN) {
                    item.setQuantity(itemOpenQuantityMap.get(item.getRootReferenceItemId()));
                }
            }

            materialService.fillMaterialDescription(inventoryDocument.getItemList());
            return inventoryDocument;
        } else if (referenceType == InventoryDocument.ReferenceTypeEnum.PO) {
            return getDocumentFromPurchaseOrder(type == InventoryDocument.TypeEnum.RETURN ? InventoryDocument.MovementTypeEnum.OUTBOUND : InventoryDocument.MovementTypeEnum.INBOUND, type, referenceType, referenceCode);
        } else if (referenceType == InventoryDocument.ReferenceTypeEnum.PDO) {
            return getDocumentFromProduceOrder(type == InventoryDocument.TypeEnum.RETURN ? InventoryDocument.MovementTypeEnum.INBOUND : InventoryDocument.MovementTypeEnum.OUTBOUND, type, referenceType, referenceCode);
        }

        return null;
    }

    private InventoryDocument getDocumentFromPurchaseOrder(InventoryDocument.MovementTypeEnum movementType, InventoryDocument.TypeEnum type, InventoryDocument.ReferenceTypeEnum referenceType, String referenceCode) {
        PurchaseOrder purchaseOrder = purchaseOrderDAO.selectByCode(referenceCode)
                .orElseThrow(() -> new BizException(ExceptionCodeEnum.PO_DOCUMENT_NOT_FOUND_ERROR, new Object[]{referenceCode}));
        InventoryDocument inventoryDocument = InventoryDocument.builder()
                .type(type)
                .referenceType(referenceType)
                .referenceCode(referenceCode)
                .rootReferenceCode(referenceCode)
                .plantId(purchaseOrder.getPlantId())
                .operatorId(UserContextHolder.get().getId())
                .documentDate(LocalDate.now())
                .build();

        inventoryDocument.setItemList(Lists.newArrayListWithExpectedSize(purchaseOrder.getItemList().size()));

        for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
            inventoryDocument.getItemList()
                    .add(InventoryDocument.Item.builder()
                            .type(type)
                            .referenceType(referenceType)
                            .referenceCode(referenceCode)
                            .referenceItemId(item.getId())
                            .rootReferenceCode(referenceCode)
                            .rootReferenceItemId(item.getId())
                            .movementType(InventoryDocument.MovementTypeEnum.INBOUND)
                            .plantId(purchaseOrder.getPlantId())
                            .materialId(item.getMaterialId())
                            .quantity(item.getQuantity())
                            .unit(item.getUnit())
                            .build());
        }

        Map<Long, BigDecimal> itemOpenQuantityMap = purchaseOrderService.openQuantity(movementType, referenceCode);
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setQuantity(ObjectUtils.defaultIfNull(itemOpenQuantityMap.get(item.getRootReferenceItemId()), BigDecimal.ZERO));
        }
        materialService.fillMaterialDescription(inventoryDocument.getItemList());

        return inventoryDocument;
    }

    private InventoryDocument getDocumentFromProduceOrder(InventoryDocument.MovementTypeEnum movementType, InventoryDocument.TypeEnum type, InventoryDocument.ReferenceTypeEnum referenceType, String referenceCode) {
        String sql = "select produce_bom_detail.`material_id`, produce_bom_detail.`material_id` referenceItemId,  produce_bom_detail.`material_id` rootReferenceItemId, sum(produce_bom_detail.quantity * produce_order_item.quantity) quantity, mm_material.base_unit unit from produce_order \n" +
                "inner join produce_order_item on produce_order_item.`produce_order_id` = produce_order.id \n" +
                "inner join produce_bom on produce_bom.`material_id` = produce_order_item.material_id\n" +
                "inner join produce_bom_detail on produce_bom.id = produce_bom_detail.`bom_id`\n" +
                "inner join mm_material on mm_material.id = produce_bom_detail.`material_id`\n" +
                "where produce_order.code = :referenceCode group by material_id";

        List<InventoryDocument.Item> itemList = sharpService.query(sql, Params.builder(1).pv("referenceCode", referenceCode).build(), InventoryDocument.Item.class);
        if (CollectionUtils.isEmpty(itemList)) {
            throw new BizException(ExceptionCodeEnum.PDO_DOCUMENT_NOT_FOUND_ERROR, new Object[]{referenceCode});
        }

        InventoryDocument inventoryDocument = InventoryDocument.builder()
                .type(type)
                .referenceType(referenceType)
                .referenceCode(referenceCode)
                .rootReferenceCode(referenceCode)
                .operatorId(UserContextHolder.get().getId())
                .documentDate(LocalDate.now())
                .build();

        inventoryDocument.setItemList(itemList);

        Map<Long, BigDecimal> itemOpenQuantityMap = produceOrderService.openQuantity(movementType, referenceCode);

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setType(type);
            item.setReferenceType(referenceType);
            item.setReferenceCode(referenceCode);
            item.setRootReferenceCode(referenceCode);
            item.setMovementType(InventoryDocument.MovementTypeEnum.OUTBOUND);
            item.setQuantity(ObjectUtils.defaultIfNull(itemOpenQuantityMap.get(item.getRootReferenceItemId()), BigDecimal.ZERO));
        }

        materialService.fillMaterialDescription(inventoryDocument.getItemList());

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


}

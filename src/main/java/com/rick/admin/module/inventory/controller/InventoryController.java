package com.rick.admin.module.inventory.controller;

import com.rick.admin.common.ExceptionCodeEnum;
import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.HandlerHelper;
import com.rick.admin.module.inventory.service.HandlerManager;
import com.rick.admin.module.inventory.service.InventoryDocumentService;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

            Map<Long, BigDecimal> materialOpenQuantityMap = null;
            if (type == InventoryDocument.TypeEnum.RETURN) {
                materialOpenQuantityMap = inventoryDocumentService.openQuantity(HandlerHelper.oppositeMovementType(inventoryDocument.getItemList().get(0).getMovementType()),
                        inventoryDocument.getRootReferenceCode());
            }

            Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(inventoryDocument.getItemList().stream().map(InventoryDocument.Item::getMaterialId).collect(Collectors.toSet()));

            for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
                Material material = idMaterialMap.get(item.getMaterialId());
                item.setMaterialCode(material.getCode());
                item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
                item.setUnitText(dictService.getDictByTypeAndName("unit", item.getUnit()).get().getLabel());
                item.setReferenceItemId(item.getId());
                if (type == InventoryDocument.TypeEnum.RETURN) {
                    item.setQuantity(materialOpenQuantityMap.get(item.getMaterialId()));
                }

            }

            return inventoryDocument;
        }

        return null;
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

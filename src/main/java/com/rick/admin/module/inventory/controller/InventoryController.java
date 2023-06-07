package com.rick.admin.module.inventory.controller;

import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.Time2StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * @author Rick.Xu
 * @date 2023/6/6 16:01
 */
@Controller
@RequestMapping("inventory")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class InventoryController {

    final InventoryDocumentDAO inventoryDocumentDAO;

    @GetMapping("move")
    public String gotoInventoryPage() {
        return "modules/inventory/index";
    }

    @PostMapping
    @ResponseBody
    public Result<String> post(@RequestBody InventoryDocument inventoryDocument) {
        inventoryDocument.setCode("MD" + Time2StringUtils.format(Instant.now()).replaceAll("\\s+|-|:", ""));
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setReferenceType(inventoryDocument.getReferenceType());
            item.setReferenceCode(inventoryDocument.getReferenceCode());
            item.setMovementType(InventoryDocument.MovementTypeEnum.INBOUND);
            item.setPlantId(inventoryDocument.getPlantId());
        }
        inventoryDocumentDAO.insert(inventoryDocument);
        return ResultUtils.success(inventoryDocument.getCode());
    }

}

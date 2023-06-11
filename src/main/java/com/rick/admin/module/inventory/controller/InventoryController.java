package com.rick.admin.module.inventory.controller;

import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.HandlerManager;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

}

package com.rick.admin.module.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Rick.Xu
 * @date 2023/6/6 16:01
 */
@Controller
@RequestMapping("inventory")
public class InventoryController {

    @GetMapping("move")
    public String gotoInventoryPage() {
        return "modules/inventory/index";
    }
}

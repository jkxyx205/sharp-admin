package com.rick.admin.module.purchase.controller;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/19 16:35
 */
@Controller
@RequestMapping("purchase_order")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PurchaseOrderController {

    final PurchaseOrderService purchaseOrderService;

    final PurchaseOrderDAO purchaseOrderDAO;

    final MaterialDAO materialDAO;

    final DictService dictService;

    @PostMapping
    @ResponseBody
    public PurchaseOrder saveOrUpdate(@RequestBody PurchaseOrder purchaseOrder) {
        purchaseOrderService.saveOrUpdate(purchaseOrder);
        return purchaseOrder;
    }

    @GetMapping
    public String gotoDetailPage(Model model) {
        return gotoDetailPage(null, model);
    }

    @GetMapping("{id}")
    public String gotoDetailPage(@PathVariable Long id, Model model) {
        if (id != null) {
            PurchaseOrder purchaseOrder = purchaseOrderDAO.selectById(id).orElseThrow(() -> new ResourceNotFoundException());
            Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(purchaseOrder.getItemList().stream().map(PurchaseOrder.Item::getMaterialId).collect(Collectors.toSet()));

            for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
                Material material = idMaterialMap.get(item.getMaterialId());
                item.setMaterialCode(material.getCode());
                item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
                item.setUnitText(dictService.getDictByTypeAndName("unit", item.getUnit()).get().getLabel());
            }

            model.addAttribute("po", purchaseOrder);
        } else {
            model.addAttribute("po", new PurchaseOrder());
        }

        return "modules/purchase/purchase_order";
    }
}

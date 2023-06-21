package com.rick.admin.module.purchase.controller;

import com.google.common.collect.Lists;
import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
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
        return gotoDetailPageById(null, model);
    }

    @GetMapping("code/{code}")
    public String gotoDetailPageByCode(@PathVariable String code, Model model) {
        return gotoDetailPageById(purchaseOrderDAO.selectIdByCode(code).orElseThrow(() -> new ResourceNotFoundException()), model);
    }

    @GetMapping("{id}")
    public String gotoDetailPageById(@PathVariable Long id, Model model) {
        PurchaseOrder purchaseOrder;
        if (id != null) {
            purchaseOrder = purchaseOrderDAO.selectById(id).orElseThrow(() -> new ResourceNotFoundException());
            Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(purchaseOrder.getItemList().stream().map(PurchaseOrder.Item::getMaterialId).collect(Collectors.toSet()));

            for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
                Material material = idMaterialMap.get(item.getMaterialId());
                item.setMaterialCode(material.getCode());
                item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
                item.setUnitText(dictService.getDictByTypeAndName("unit", item.getUnit()).get().getLabel());
            }

            model.addAttribute("po", purchaseOrder);
            // 收货记录
            List<GoodsReceiptItem> goodsReceiptItemList = Lists.newArrayListWithExpectedSize(purchaseOrder.getItemList().size());

            Map<Long, BigDecimal> itemOpenQuantityMap = purchaseOrderService.openQuantity(InventoryDocument.MovementTypeEnum.INBOUND, purchaseOrder.getCode());

            for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
                GoodsReceiptItem goodsReceiptItem = new GoodsReceiptItem();
                BeanUtils.copyProperties(item, goodsReceiptItem);
                goodsReceiptItem.openQuantity = itemOpenQuantityMap.get(item.getId());
                goodsReceiptItemList.add(goodsReceiptItem);
            }
            model.addAttribute("goodsReceiptItemList", goodsReceiptItemList);
        } else {
            purchaseOrder = new PurchaseOrder();
            purchaseOrder.setCreateBy(UserContextHolder.get().getId());
            model.addAttribute("po", purchaseOrder);
        }

        model.addAttribute("createName", dictService.getDictByTypeAndName("sys_user", purchaseOrder.getCreateBy().toString()).get().getLabel());
        model.addAttribute("createTime", Time2StringUtils.format(purchaseOrder.getCreateTime()));

        return "modules/purchase/purchase_order";
    }

//    @GetMapping("{id}/gr")
//    public List<GoodsReceiptItem> gotoGoodsReceiptTable(@PathVariable Long id) {
//        PurchaseOrder purchaseOrder = purchaseOrderDAO.selectById(id).orElseThrow(() -> new ResourceNotFoundException());
//
//        List<GoodsReceiptItem> list = Lists.newArrayListWithExpectedSize(purchaseOrder.getItemList().size());
//
//        Map<Long, BigDecimal> itemOpenQuantityMap = purchaseOrderService.openQuantity(InventoryDocument.MovementTypeEnum.INBOUND, purchaseOrder.getCode());
//
//        for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
//            GoodsReceiptItem goodsReceiptItem = new GoodsReceiptItem();
//            BeanUtils.copyProperties(item, goodsReceiptItem);
//            goodsReceiptItem.openQuantity = itemOpenQuantityMap.get(item.getId());
//            list.add(goodsReceiptItem);
//        }
//
//        return list;
//    }

    @DeleteMapping("{id}")
    @ResponseBody
    public Result<Integer> deleteById(@PathVariable Long id) {
        return ResultUtils.success(purchaseOrderDAO.deleteById(id));
    }

    class GoodsReceiptItem extends PurchaseOrder.Item {

        private BigDecimal openQuantity;

        public BigDecimal getGoodsReceiptQuantity() {
            return getQuantity().subtract(ObjectUtils.defaultIfNull(openQuantity, BigDecimal.ZERO));
        }

        public BigDecimal getOpenQuantity() {
            return openQuantity;
        }

    }
}

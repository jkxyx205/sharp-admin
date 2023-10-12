package com.rick.admin.module.purchase.controller;

import com.google.common.collect.Lists;
import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.core.dao.PartnerDAO;
import com.rick.admin.module.core.entity.ContactInfo;
import com.rick.admin.module.core.entity.Partner;
import com.rick.admin.module.core.model.ReferenceTypeEnum;
import com.rick.admin.module.core.service.ContactInfoService;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import com.rick.admin.module.purchase.service.PurchaseRequisitionItemService;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
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

    final MaterialService materialService;

    final PartnerDAO partnerDAO;

    final BatchService batchService;

    final ContactInfoService contactInfoService;

    final PurchaseRequisitionItemService purchaseRequisitionItemService;

    @PostMapping
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder saveOrUpdate(@RequestBody PurchaseOrder purchaseOrder) {
        if (PurchaseOrder.StatusEnum.DONE == purchaseOrder.getStatus()) {
            purchaseOrder.getItemList().forEach(item -> item.setComplete(true));
        } else if (purchaseOrder.getItemList().stream().allMatch(item -> item.getComplete())) {
            purchaseOrder.setStatus(PurchaseOrder.StatusEnum.DONE);
        }

        purchaseOrderService.saveOrUpdate(purchaseOrder);

        // 更新物料的价格
        List<Object[]> paramList = purchaseOrder.getItemList().stream().filter(item -> Objects.nonNull(item.getUnitPrice()))
        .map(item -> new Object[]{item.getUnitPrice(), item.getMaterialId()}).collect(Collectors.toList());
        materialDAO.updatePrice(paramList);
        materialService.fillMaterialDescription(purchaseOrder.getItemList());

        return purchaseOrder;
    }

    @GetMapping("batch")
    public String gotoBatch() {
        return "modules/purchase/purchase_order_batch";
    }

    @PostMapping("batch")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result batchSave(@RequestBody Map<String, Object> value) {
        List<Map> list = (List<Map>) value.get("itemList");
        Map<String, List<Map>> partnerIdMap = list.stream().collect(Collectors.groupingBy(r -> (String) r.get("partnerId")));
        List<PurchaseOrder> purchaseOrderList = Lists.newArrayListWithExpectedSize(partnerIdMap.size());

        Map<Long, Partner> partnerMap = partnerDAO.selectByIdsAsMap(partnerIdMap.keySet());

        for (Map.Entry<String, List<Map>> entry : partnerIdMap.entrySet()) {
            value.put("partnerId", entry.getKey());
            value.put("itemList", entry.getValue());
            // 添加供应商联系人和联系方式
            Partner partner = partnerMap.get(Long.parseLong(entry.getKey()));
            value.put("contactPerson", partner.getContactPerson());
            value.put("contactNumber", partner.getContactNumber());
            value.put("contactMail", partner.getContactMail());
            value.put("status", PurchaseOrder.StatusEnum.PLANNING);
            PurchaseOrder purchaseOrder = purchaseOrderDAO.mapToEntity(value);

            // TODO 添加参考信息 SO
            purchaseOrderList.add(purchaseOrder);
        }

        handleReferenceValue(purchaseOrderList.stream().flatMap(purchaseOrder -> purchaseOrder.getItemList().stream())
                .filter(purchaseOrderItem -> purchaseOrderItem.getReferenceType1() == ReferenceTypeEnum.PR).collect(Collectors.toList()));
        // 更新物料的价格
//        List<Object[]> paramList = purchaseOrderList.stream().flatMap(purchaseOrder -> purchaseOrder.getItemList().stream()).filter(item -> Objects.nonNull(item.getUnitPrice()))
//                .map(item -> new Object[]{item.getUnitPrice(), item.getMaterialId()}).collect(Collectors.toList());
//        materialDAO.updatePrice(paramList);

        purchaseOrderService.save(purchaseOrderList);
        return ResultUtils.success();
    }

    @DeleteMapping("requisition")
    @ResponseBody
    public Result deletePurchaseRequisitionByIds(String ids) {
        purchaseRequisitionItemService.deleteByIds(ids);
        return ResultUtils.success();
    }

    private void handleReferenceValue(List<PurchaseOrder.Item> itemList) {
        if (CollectionUtils.isEmpty(itemList)) {
            return;
        }

        Set<Long> produceItemIds = itemList.stream().map(PurchaseOrder.Item::getReferenceId2).collect(Collectors.toSet());
        Set<Long> prItemIds = itemList.stream().map(PurchaseOrder.Item::getReferenceId1).collect(Collectors.toSet());
        Map<Long, ContactInfo> instanceIdEntityMap = contactInfoService.getInstanceIdEntityMap(produceItemIds);

        for (PurchaseOrder.Item item : itemList) {
            BaseEntityUtils.resetAdditionalFields(item);
            ContactInfo contactInfo = instanceIdEntityMap.get(item.getReferenceId2());
            if (Objects.nonNull(contactInfo)) {
                BaseEntityUtils.resetAdditionalFields(contactInfo);
                // TODO 销售订单如果修改了地址，采购订单的地址该如何更新？
                item.setContactInfo(contactInfo);
            }
        }

        purchaseRequisitionItemService.markCompleted(prItemIds);
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
            materialService.fillMaterialDescription(purchaseOrder.getItemList());

            model.addAttribute("po", purchaseOrder);

            // 收货记录
            List<GoodsReceiptItem> goodsReceiptItemList = Lists.newArrayListWithExpectedSize(purchaseOrder.getItemList().size());

            Map<Long, BigDecimal> historyGoodsReceiptQuantityMap = purchaseOrderService.historyGoodsReceiptQuantity(purchaseOrder.getCode());

            for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
                GoodsReceiptItem goodsReceiptItem = new GoodsReceiptItem();
                BeanUtils.copyProperties(item, goodsReceiptItem);
                goodsReceiptItem.goodsReceiptQuantity = historyGoodsReceiptQuantityMap.get(item.getId());
                goodsReceiptItemList.add(goodsReceiptItem);

            }

            batchService.fillCharacteristicValue(purchaseOrder.getItemList());
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

    /**
     * 获取物料的供应商
     * @return
     */
    @GetMapping("materials/{id}/vendors")
    @ResponseBody
    public List<Dict> getVendorByMaterialId(@PathVariable Long id) {
        return purchaseOrderService.getVendorByMaterialId(id);
    }

    @GetMapping("materials/vendors")
    @ResponseBody
    public Map<String, List<Dict>> getVendorByMaterialId(@RequestParam String materialIds) {
        Set<Long> materialIdSet = Arrays.asList(materialIds.split(",")).stream().map(r -> Long.parseLong(r)).collect(Collectors.toSet());
        return purchaseOrderService.getVendorByMaterialIds(materialIdSet);
    }

    @DeleteMapping("{id}")
    @ResponseBody
    public Result<Integer> deleteById(@PathVariable Long id) {
        return ResultUtils.success(purchaseOrderDAO.deleteById(id));
    }

    @GetMapping("{id}/download")
    public void downloadById(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        purchaseOrderService.downloadById(id, request, response);
    }

    class GoodsReceiptItem extends PurchaseOrder.Item {

        private BigDecimal goodsReceiptQuantity;

        public BigDecimal getGoodsReceiptQuantity() {
            return goodsReceiptQuantity;
        }

        public BigDecimal getOpenQuantity() {
            BigDecimal value = getQuantity().subtract(ObjectUtils.defaultIfNull(goodsReceiptQuantity, BigDecimal.ZERO));
            return BigDecimalUtils.lt(value, BigDecimal.ZERO) ? BigDecimal.ZERO : value;
        }

    }
}

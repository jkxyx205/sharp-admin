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
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.BatchSupport;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.entity.PurchaseRequisition;
import com.rick.admin.module.purchase.service.LatestPriceService;
import com.rick.admin.module.purchase.service.PurchaseOrderService;
import com.rick.admin.module.purchase.service.PurchaseRequisitionItemService;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

    final DictService dictService;

    final MaterialService materialService;

    final PartnerDAO partnerDAO;

    final BatchService batchService;

    final ContactInfoService contactInfoService;

    final PurchaseRequisitionItemService purchaseRequisitionItemService;

    final LatestPriceService latestPriceService;

    final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    final SharpService sharpService;

    @PostMapping
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder saveOrUpdate(@RequestBody PurchaseOrder purchaseOrder) {
        purchaseOrderService.saveOrUpdate(purchaseOrder);
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

        purchaseOrderService.save(purchaseOrderList);

        Set<String> deletedKeys = handleOverPurchasedMaterialKeys(purchaseOrderList.stream().flatMap(purchaseOrder -> purchaseOrder.getItemList().stream())
                .filter(purchaseOrderItem -> purchaseOrderItem.getReferenceType1() != ReferenceTypeEnum.PR).collect(Collectors.toList()));

        Set<Long> prItemIds = purchaseOrderList.stream().flatMap(purchaseOrder -> purchaseOrder.getItemList().stream())
                .filter(purchaseOrderItem -> purchaseOrderItem.getReferenceType1() == ReferenceTypeEnum.PR).collect(Collectors.toList()).stream().map(PurchaseOrder.Item::getReferenceId1).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(prItemIds)) {
            purchaseRequisitionItemService.markCompleted(prItemIds);
        }

        // 删除重复的采购申请
        if (CollectionUtils.isNotEmpty(deletedKeys)) {
            namedParameterJdbcTemplate.update("UPDATE pur_purchase_requisition_item SET is_deleted = 1 WHERE concat(material_id, IFNULL(batch_id, '')) IN (:keys) and is_complete = 0",
                    Params.builder(1).pv("keys", deletedKeys).build());
        }

        return ResultUtils.success();
    }

    private Set<String> handleOverPurchasedMaterialKeys(List<PurchaseOrder.Item> itemList) {
        if (CollectionUtils.isEmpty(itemList)) {
            return null;
        }

        Map<String, Integer> requisitionQuantityMapping = purchaseRequisitionItemService.requisitionQuantityMapping();
        Map<String, Integer> purchaseQuantityMapping = itemList.stream().collect(Collectors.groupingBy(item -> item.getMaterialId() + (Objects.isNull(item.getBatchId()) ? "" : "" + item.getBatchId()),
                Collectors.summingInt(item -> item.getQuantity().intValue()))
        );

        Set<String> deletedKeys = new HashSet<>();
        Set<Long> deletedRequisitionIds = new HashSet<>();

        for (Map.Entry<String, Integer> purchaseQuantityEntry : purchaseQuantityMapping.entrySet()) {
            String key = purchaseQuantityEntry.getKey();
            Integer quantity = purchaseQuantityEntry.getValue();

            if (requisitionQuantityMapping.get(key) != null && quantity >= requisitionQuantityMapping.get(key)) {
                // 标记删除全部申请记录
                deletedKeys.add(key);
            } else if (requisitionQuantityMapping.get(key) != null && quantity < requisitionQuantityMapping.get(key)) {
                // 删除部分采购申请记录
                List<PurchaseRequisition.Item> items = purchaseRequisitionItemService.requisitionItemList(key);
                if (CollectionUtils.isNotEmpty(items)) {
                    BigDecimal count = BigDecimal.ZERO;
                    for (PurchaseRequisition.Item item : items) {
                        count = count.add(item.getQuantity());

                        if (count.intValue() > quantity) {
                            break;
                        }
                        deletedRequisitionIds.add(item.getId());
                    }
                }

            }
        }

        if (CollectionUtils.isNotEmpty(deletedRequisitionIds)) {
             purchaseRequisitionItemService.deleteByIds(deletedRequisitionIds);
        }

        return deletedKeys;
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
        model.addAttribute("customerContactInfoMap", customerContactInfoMapping());
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

    @PostMapping("/latest-price")
    @ResponseBody
    public Result<String> getLatestPrice(Long materialId, @RequestBody List<Classification> classificationList, Long partnerId) {
        Optional<BigDecimal> optional = latestPriceService.getLatestPrice(materialId,
                latestPriceService.priceBatchCode(materialId, BatchSupport.characteristicToCode(classificationList), classificationList), partnerId);
        return ResultUtils.success(optional.isPresent() ? optional.get().stripTrailingZeros().toPlainString() : "");
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

    private Map<String, Map<String, Object>> customerContactInfoMapping() {
        String querySql = "select id, contact_person contactPerson, contact_number contactNumber, contact_mail contactMail from core_partner where partner_type = 'VENDOR'";
        List<Map<String, Object>> list = sharpService.query(querySql, null);
        return list.stream().collect(Collectors.toMap(row -> Objects.toString(row.get("id"), ""), row -> row));
    }
}

package com.rick.admin.module.produce.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.core.model.ReferenceTypeEnum;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.admin.module.material.service.MaterialDescriptionHandler;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.dao.ProduceOrderItemDAO;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.BomService;
import com.rick.admin.module.produce.service.ProduceOrderService;
import com.rick.admin.module.produce.service.SpecialMaterialConstant;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.admin.module.purchase.entity.PurchaseRequisition;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.support.BaseEntityUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Rick.Xu
 * @date 2023/7/11 11:46
 */
@Controller
@RequestMapping("produce_orders")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProduceOrderController {

    EntityCodeDAO<ProduceOrder, Long> produceOrderDAO;

    ProduceOrderItemDAO produceOrderItemDAO;

    EntityDAO<PurchaseRequisition.Item, Long> purchaseRequisitionItemDAO;

    ProduceOrderService produceOrderService;

    SharpService sharpService;

    MaterialService materialService;

    BomService bomService;

    BatchService batchService;

    DictService dictService;

    /**
     * 根据物料 ID 获取 bom
     * @param materialId
     * @return
     */
    @GetMapping("bom")
    @ResponseBody
    public Map<String, Object> gotoBomForm(@RequestParam Long materialId, Long itemId, Boolean isCopy) {
        ProduceOrder.Item item = null;
        if (Objects.nonNull(itemId)) {
            item = produceOrderItemDAO.selectById(itemId).get();
        }

        BomTemplate bomTemplate = gotoBomForm(materialId, item, isCopy);
        if (isCopy && Objects.nonNull(item)) {
            materialService.fillMaterialDescription(Stream.of(item).collect(Collectors.toSet()));
            item.setUnitPrice(null);
            item.setQuantity(null);
            item.setDeliveryDate(null);
            BaseEntityUtils.resetAdditionalFields(item);
        }
        return Params.builder(2).pv("bomTemplate", bomTemplate).pv("item", item).build();
    }

    @GetMapping("code/{code}")
    public String gotoDetailPageByCode(@PathVariable String code, Model model) {
        return gotoDetailPageById(produceOrderDAO.selectIdByCode(code).orElseThrow(() -> new ResourceNotFoundException()), model);
    }

    /**
     * 新建页面
     * @param model
     * @return
     */
    @GetMapping("new")
    public String gotoDetailPage(Model model) {
        model.addAttribute("customerContactInfoMap", customerContactInfoMapping());
        return gotoDetailPageById(null, model);
    }

    /**
     * 根据 id 获取实例信息并跳转页面
     * @param id
     * @param model
     * @return
     */
    @GetMapping("{id}")
    public String gotoDetailPageById(@PathVariable Long id, Model model) {
        ProduceOrder produceOrder;
        if (Objects.nonNull(id)) {
            produceOrder = produceOrderDAO.selectById(id).orElseThrow(() -> new ResourceNotFoundException());
            Map<Long, BomTemplate> itemIdBomTemplateMap = Maps.newHashMapWithExpectedSize(produceOrder.getItemList().size());

            for (ProduceOrder.Item item : produceOrder.getItemList()) {
                if (CollectionUtils.isNotEmpty(item.getItemList())) {
                    BomTemplate bomTemplate = gotoBomForm(item.getMaterialId(), item, false);
                    itemIdBomTemplateMap.put(item.getId(), bomTemplate);
                } else {
                    batchService.fillCharacteristicValue(Arrays.asList(item));
                }
            }

            model.addAttribute("po", produceOrder);
            model.addAttribute("bomTemplate", itemIdBomTemplateMap);

            // 领料记录
            List<GoodsReceiptItem> goodsReceiptItemList = getGoodsReceiptItemList(produceOrder.getCode());
//            model.addAttribute("goodsReceiptItemList", goodsReceiptItemList);

            // 采购订单
            List<RelatedPurchaseOrder> relatedPurchaseOrderList = getRelatedPurchaseOrders(id);
            model.addAttribute("relatedPurchaseOrderList", relatedPurchaseOrderList);

            materialService.fillMaterialDescription(Stream.concat(Stream.concat(produceOrder.getItemList().stream(), goodsReceiptItemList.stream()), relatedPurchaseOrderList.stream()).collect(Collectors.toList()));
//            materialService.fillMaterialDescription(produceOrder.getItemList());

            // 展示按生产单号领取的物料
            model.addAttribute("goodsReceiptItemList", goodsReceiptItemList.stream().filter(item -> item.getMaterialDescription().getCategoryId() != null && SpecialMaterialConstant.isSpecialSpecialMaterialCategory(item.getMaterialDescription().getCategoryId())).collect(Collectors.toList()));

            // 生产计划
            for (ProduceOrder.Item item : produceOrder.getItemList()) {
                if (CollectionUtils.isNotEmpty(item.getScheduleList())) {
                    for (ProduceOrder.Item.Schedule schedule : item.getScheduleList()) {
                        schedule.setUnitText(item.getUnitText());
                    }
                }
            }

            // 发货记录
            List<ProduceOrderController.GoodsIssueItem> goodsIssueItemList = Lists.newArrayListWithExpectedSize(produceOrder.getItemList().size());

            Map<Long, BigDecimal> historyGoodsIssueQuantityMap = produceOrderService.salesHistoryGoodsIssueQuantity(produceOrder.getCode(), produceOrder.getItemList().stream().map(ProduceOrder.Item::getId).collect(Collectors.toSet()));

            for (ProduceOrder.Item item : produceOrder.getItemList()) {
                ProduceOrderController.GoodsIssueItem goodsIssueItem = new ProduceOrderController.GoodsIssueItem();
                BeanUtils.copyProperties(item, goodsIssueItem);
                goodsIssueItem.goodsIssueQuantity = historyGoodsIssueQuantityMap.get(item.getId());
                goodsIssueItemList.add(goodsIssueItem);
            }
            model.addAttribute("goodsIssueItemList", goodsIssueItemList);
        } else {
            produceOrder = new ProduceOrder();
            produceOrder.setCreateBy(UserContextHolder.get().getId());
            model.addAttribute("po", produceOrder);
            model.addAttribute("bomTemplate", Collections.emptyMap());
        }

        model.addAttribute("createName", dictService.getDictByTypeAndName("sys_user", produceOrder.getCreateBy().toString()).get().getLabel());
        model.addAttribute("createTime", Time2StringUtils.format(produceOrder.getCreateTime()));
        return "modules/produce/produce_order";
    }

    private List<RelatedPurchaseOrder> getRelatedPurchaseOrders(long produceOrderId) {
        return sharpService.query("select material_id materialId, material_code materialCode, batch_id batchId, batch_code batchCode, quantity, purchase_order_code purchaseOrderCode, create_time from pur_purchase_order_item where reference_type2 = 'SO' AND produce_order_id = :produceOrderId",
                Params.builder(1).pv("produceOrderId", produceOrderId).build(), RelatedPurchaseOrder.class);
    }

    private List<ProduceOrderController.GoodsReceiptItem> getGoodsReceiptItemList(String produceOrderCode) {
        // language=SQL
        String sql = "select '${produceOrderCode}'                    produceOrderCode,\n" +
                "       mm_material.id                         material_id,\n" +
                "       mm_material.code                       materialCode,\n" +
                "       t1.batch_id,\n" +
                "       t1.batch_code,\n" +
                "       t1.id rootReferenceItemId," +
                "       mm_material.base_unit                  unitText,\n" +
                "       t1.quantity,\n" +
                "       IFNULL(t2.quantity, 0)                 goodsReceiptQuantity,\n" +
                "       (t1.quantity - IFNULL(t2.quantity, 0)) openQuantity\n" +
                "from (select produce_order_item_detail.`id`,\n" +
                "             produce_order_item_detail.material_id,\n" +
                "             produce_order_item_detail.batch_id,\n" +
                "             produce_order_item_detail.batch_code,\n" +
                "             (CASE " +
                "               WHEN produce_order_item_detail.component_detail_id = 725451860537794560 THEN 3 * produce_order_item_detail.quantity" +
                "               WHEN produce_order_item_detail.component_detail_id = 725451860537794561 THEN 3 * produce_order_item_detail.quantity" +
                "               ELSE produce_order_item_detail.quantity END) * produce_order_item.quantity quantity\n" +
                "      from produce_order\n" +
                "               inner join produce_order_item on produce_order_item.`produce_order_id` = produce_order.id\n" +
                "               inner join produce_order_item_detail on produce_order_item.id = produce_order_item_detail.`produce_order_item_id`\n" +
                "      where produce_order.code = :produceOrderCode) t1\n" +
                "         left join (select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity\n" +
                "                    from inv_document_item\n" +
                "                    where exists (select 1 from produce_order_item_schedule where produce_order_code = :produceOrderCode AND inv_document_item.root_reference_code = produce_order_item_schedule.code) AND plant_id = 719893335619162112" +
                "                    group by root_reference_item_id) t2 on t1.id = t2.root_reference_item_id\n" +
                "         left join `mm_material` on mm_material.id = t1.material_id";

        List<GoodsReceiptItem> goodsReceiptItemList = sharpService.query(sql, Params.builder(1).pv("produceOrderCode", produceOrderCode).build(), GoodsReceiptItem.class);
        if (CollectionUtils.isNotEmpty(goodsReceiptItemList)) {
//            Set<String> materialIdBatchIdStringCollection = goodsReceiptItemList.stream().map(goodsReceiptItem -> MaterialProfileSupport.materialIdBatchIdString(goodsReceiptItem.getMaterialId(), goodsReceiptItem.getBatchId())).collect(Collectors.toSet());
//            Map<String, String> characteristicTextMap = materialProfileService.getCharacteristicText(materialIdBatchIdStringCollection);
            for (GoodsReceiptItem item : goodsReceiptItemList) {
                item.setOpenQuantity(BigDecimalUtils.lt(item.getOpenQuantity(), BigDecimal.ZERO) ? BigDecimal.ZERO : item.getOpenQuantity());
//                item.setCharacteristic(characteristicTextMap.get(MaterialProfileSupport.materialIdBatchIdString(item.getMaterialId(), item.getBatchId())));
            }
        }

        return goodsReceiptItemList;
    }

    /**
     * 修改或更新
     * @param produceOrder
     * @return
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public ProduceOrder saveOrUpdate(@RequestBody ProduceOrder produceOrder) {
        if (!UserContextHolder.get().getAuthorityList().contains("produce_order_add")) {
            // 回写到 produceOrder
            ProduceOrder produceOrderInDb = produceOrderDAO.selectByParamsWithoutCascade(ProduceOrder.builder().id(produceOrder.getId()).build()).get(0);
            produceOrder.setPartnerId(produceOrderInDb.getPartnerId());
            produceOrder.setContactPerson(produceOrderInDb.getContactPerson());
            produceOrder.setContactNumber(produceOrderInDb.getContactNumber());
            produceOrder.setContactMail(produceOrderInDb.getContactMail());
            produceOrder.setSourceOrderNum(produceOrderInDb.getSourceOrderNum());
        }
        produceOrderService.saveOrUpdate(produceOrder);
        return produceOrder;
    }

    @GetMapping("purchase_order")
    public String batch(Model model, String materialIds, String quantity) {
        List<PurchaseOrder.Item> itemList = new ArrayList<>();

        String[] quantityArr = quantity.split(",");
        String[] materialIdArr = materialIds.split(",");
                
        for (int i = 0; i < materialIdArr.length; i++) {
            String mc = materialIdArr[i];
            String[] split = mc.split("@");
            long materialId = Long.parseLong(split[0]);
            PurchaseOrder.Item item = new PurchaseOrder.Item();
            item.setMaterialId(materialId);
            if (split.length > 1) {
                item.setBatchId(Long.parseLong(split[1]));
            }

            item.setQuantity(new BigDecimal(quantityArr[i]));
            itemList.add(item);
        }

        handItemExtraValue(itemList);
        model.addAttribute("itemList", itemList);
        return "modules/purchase/purchase_order_batch";
    }

    @GetMapping("purchase_order_purchase_send")
    public String batch(Model model, Long[] itemIds) {
        List<PurchaseOrder.Item> itemList = new ArrayList<>();

        List<PurchaseRequisition.Item> prItemList = purchaseRequisitionItemDAO.selectByIds(itemIds);

        for (PurchaseRequisition.Item soItem : prItemList) {
            PurchaseOrder.Item poItem = new PurchaseOrder.Item();
            BeanUtils.copyProperties(soItem, poItem);
            poItem.setReferenceType1(ReferenceTypeEnum.PR);
            poItem.setReferenceId1(soItem.getId());
            poItem.setReferenceType2(ReferenceTypeEnum.SO);
            poItem.setReferenceId2(soItem.getReferenceId());
            poItem.setProduceOrderId(soItem.getReferenceDocumentId());
            poItem.setPurchaseSend(soItem.getPurchaseSend());
            itemList.add(poItem);
        }

        handItemExtraValue(itemList);
        model.addAttribute("itemList", itemList);
        return "modules/purchase/purchase_order_batch";
    }

    private void handItemExtraValue(List<PurchaseOrder.Item> itemList) {
        materialService.fillMaterialDescription(itemList);

        itemList.forEach(item -> {
            item.setMaterialCode(item.getMaterialDescription().getCode());
            item.setUnit(item.getMaterialDescription().getUnit());
            item.setUnitPrice(item.getMaterialDescription().getUnitPrice());
        });

        batchService.handleClassificationAndFillCharacteristicValue(itemList);
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    @ResponseBody
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.success(produceOrderDAO.deleteById(id));
    }

    @Data
    public static class RelatedPurchaseOrder implements MaterialDescriptionHandler {

        private String purchaseOrderCode;

        private Long materialId;

        private String materialCode;

        private BigDecimal quantity;

        private MaterialDescription materialDescription;

        private Long batchId;

        private String batchCode;

        private LocalDateTime createTime;

    }

    @Data
    public static class GoodsReceiptItem implements MaterialDescriptionHandler {

        private String produceOrderCode;

        private String rootReferenceItemId;

        private Long materialId;

        private String materialCode;

        private BigDecimal quantity;

        private BigDecimal goodsReceiptQuantity;

        private BigDecimal openQuantity;

        private MaterialDescription materialDescription;

        private Long batchId;

        public Boolean getComplete() {
            return BigDecimalUtils.eq(openQuantity, BigDecimal.ZERO);
        }

    }

    class GoodsIssueItem extends ProduceOrder.Item {

        private BigDecimal goodsIssueQuantity;

        public BigDecimal getGoodsIssueQuantity() {
            return goodsIssueQuantity;
        }

        public BigDecimal getOpenQuantity() {
            BigDecimal value = getQuantity().subtract(ObjectUtils.defaultIfNull(goodsIssueQuantity, BigDecimal.ZERO));
            return BigDecimalUtils.lt(value, BigDecimal.ZERO) ? BigDecimal.ZERO : value;
        }

    }

    private BomTemplate gotoBomForm(@RequestParam Long materialId, ProduceOrder.Item item, Boolean isCopy) {
        Map<Long, ProduceOrder.Item.Detail> valueMapping = Collections.EMPTY_MAP;

        if (Objects.nonNull(item)) {
            batchService.fillCharacteristicValue(Stream.concat(item.getItemList().stream(), Stream.of(item)).collect(Collectors.toSet()));
            valueMapping = item.getItemList().stream().collect(Collectors.toMap(ProduceOrder.Item.Detail::getComponentDetailId, v -> v));
        }

        BomTemplate bomTemplate = bomService.getBomTemplateMaterialId(materialId, valueMapping, isCopy);

        return bomTemplate;
    }

    private Map<String, Map<String, Object>> customerContactInfoMapping() {
        String querySql = "select id, contact_person contactPerson, contact_number contactNumber, contact_mail contactMail from core_partner where partner_type = 'CUSTOMER'";
        List<Map<String, Object>> list = sharpService.query(querySql, null);
        return list.stream().collect(Collectors.toMap(row -> Objects.toString(row.get("id"), ""), row -> row));
    }
}

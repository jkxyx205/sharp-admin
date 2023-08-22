package com.rick.admin.module.produce.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.material.dao.ClassificationDAO;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.*;
import com.rick.admin.module.produce.dao.ProduceOrderItemDAO;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.BomService;
import com.rick.admin.module.produce.service.ProduceOrderService;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
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
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    ProduceOrderService produceOrderService;

    SharpService sharpService;

    MaterialService materialService;

    BomService bomService;

    MaterialProfileService materialProfileService;

    BatchService batchService;

    ClassificationDAO materialClassificationDAO;

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
                BomTemplate bomTemplate = gotoBomForm(item.getMaterialId(), item, false);
                itemIdBomTemplateMap.put(item.getId(), bomTemplate);
            }

            model.addAttribute("po", produceOrder);
            model.addAttribute("bomTemplate", itemIdBomTemplateMap);

            // 领料记录
            List<GoodsReceiptItem> goodsReceiptItemList = getGoodsReceiptItemList(produceOrder.getCode());
            model.addAttribute("goodsReceiptItemList", goodsReceiptItemList);

            materialService.fillMaterialDescription(Stream.concat(produceOrder.getItemList().stream(), goodsReceiptItemList.stream()).collect(Collectors.toSet()));

            // 发货记录
            List<ProduceOrderController.GoodsIssueItem> goodsIssueItemList = Lists.newArrayListWithExpectedSize(produceOrder.getItemList().size());

            Map<Long, BigDecimal> historyGoodsIssueQuantityMap = produceOrderService.historyGoodsIssueQuantity(produceOrder.getCode(), produceOrder.getItemList().stream().map(ProduceOrder.Item::getId).collect(Collectors.toSet()));

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
        return "modules/produce_order";
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
                "             produce_order_item_detail.quantity * produce_order_item.quantity quantity\n" +
                "      from produce_order\n" +
                "               inner join produce_order_item on produce_order_item.`produce_order_id` = produce_order.id\n" +
                "               inner join produce_order_item_detail on produce_order_item.id = produce_order_item_detail.`produce_order_item_id`\n" +
                "      where produce_order.code = :produceOrderCode) t1\n" +
                "         left join (select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity\n" +
                "                    from inv_document_item\n" +
                "                    where `root_reference_code` = :produceOrderCode\n" +
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

        Map<Long, List<com.rick.admin.module.material.entity.Classification>> materialIdClassificationMap = materialClassificationDAO.findMaterialClassificationByMaterialIds(itemList.stream().map(PurchaseOrder.Item::getMaterialId).collect(Collectors.toSet()));

        materialService.fillMaterialDescription(itemList);

        itemList.forEach(item -> {
            item.setMaterialCode(item.getMaterialDescription().getCode());
            item.setUnit(item.getMaterialDescription().getUnit());
            List<Classification> classificationList = materialIdClassificationMap.get(item.getMaterialId());
            if (CollectionUtils.isNotEmpty(classificationList)) {
                // 拷贝
                List<Classification> newclassificationList = Lists.newArrayListWithExpectedSize(classificationList.size());
                for (Classification classification : classificationList) {
                    newclassificationList.add(SerializationUtils.clone(classification));
                }
                classificationList = newclassificationList;
            } else {
                classificationList =  Collections.emptyList();
            }

            item.setClassificationList(classificationList);
        });

        batchService.fillCharacteristicValue(itemList);
        model.addAttribute("itemList", itemList);
        return "modules/purchase/purchase_order_batch";
    }

    /**
     * 逻辑删除
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    @ResponseBody
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.success(produceOrderDAO.deleteById(id));
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
}

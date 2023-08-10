package com.rick.admin.module.produce.controller;

import com.google.common.collect.Maps;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.BomService;
import com.rick.admin.module.produce.service.ProduceOrderService;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    EntityDAO<ProduceOrder.Item, Long> produceOrderItemDAO;

    ProduceOrderService produceOrderService;

    MaterialDAO materialDAO;

    DictService dictService;

    SharpService sharpService;

    MaterialService materialService;

    BomService bomService;

    /**
     * 根据物料 ID 获取 bom
     * @param materialId
     * @return
     */
    @GetMapping("bom")
    @ResponseBody
    public BomTemplate gotoBomForm(@RequestParam Long materialId, Long itemId) {
        Map<Long, ProduceOrder.Item.Detail> valueMapping = Collections.EMPTY_MAP;

        if (Objects.nonNull(itemId)) {
            ProduceOrder.Item item = produceOrderItemDAO.selectById(itemId).get();
            valueMapping = item.getItemList().stream().collect(Collectors.toMap(ProduceOrder.Item.Detail::getComponentDetailId, v -> v));
        }

        BomTemplate bomTemplate = bomService.getBomTemplateMaterialId(materialId, valueMapping);

        return bomTemplate;
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
        if (Objects.nonNull(id)) {
            ProduceOrder produceOrder = produceOrderDAO.selectById(id).orElseThrow(() -> new ResourceNotFoundException());

            materialService.fillMaterialDescription(produceOrder.getItemList());
            Map<Long, BomTemplate> itemIdBomTemplateMap = Maps.newHashMapWithExpectedSize(produceOrder.getItemList().size());

            for (ProduceOrder.Item item : produceOrder.getItemList()) {
                BomTemplate bomTemplate = gotoBomForm(item.getMaterialId(), item.getId());
                itemIdBomTemplateMap.put(item.getId(), bomTemplate);
            }

            model.addAttribute("po", produceOrder);
            model.addAttribute("bomTemplate", itemIdBomTemplateMap);

            // 领料记录
            model.addAttribute("goodsReceiptItemList", getGoodsReceiptItemList(produceOrder.getCode()));
        } else {
            model.addAttribute("po", new ProduceOrder());
            model.addAttribute("bomTemplate", Collections.emptyMap());
        }

        return "modules/produce_order";
    }

    private List<ProduceOrderController.GoodsReceiptItem> getGoodsReceiptItemList(String produceOrderCode) {
        // language=SQL
        String sql = "select '${produceOrderCode}'                    produceOrderCode,\n" +
                "       mm_material.id                         material_id,\n" +
                "       mm_material.code                       materialCode,\n" +
                "       t1.color,\n" +
                "       mm_material.base_unit                  unitText,\n" +
                "       t1.quantity,\n" +
                "       IFNULL(t2.quantity, 0)                 goodsReceiptQuantity,\n" +
                "       (t1.quantity - IFNULL(t2.quantity, 0)) openQuantity\n" +
                "from (select produce_order_item_detail.`id`,\n" +
                "             produce_order_item_detail.material_id,\n" +
                "             produce_order_item_detail.batch_code,\n" +
                "             produce_order_item_detail.color,\n" +
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
            Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(goodsReceiptItemList.stream().map(ProduceOrderController.GoodsReceiptItem::getMaterialId).collect(Collectors.toSet()));
            for (GoodsReceiptItem item : goodsReceiptItemList) {
                Material material = idMaterialMap.get(item.getMaterialId());
                item.setMaterialText(material.getName() + " " + material.getSpecificationText());
                item.setUnitText(dictService.getDictByTypeAndName("unit", item.getUnitText()).get().getLabel());
                item.setOpenQuantity(BigDecimalUtils.lt(item.getOpenQuantity(), BigDecimal.ZERO) ? BigDecimal.ZERO : item.getOpenQuantity());
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
        Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(materialIds);

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
                sharpService.queryForObject("select mm_batch.id, mm_batch.code, mm_characteristic_value.`value` from mm_profile left join mm_batch on code = `mm_profile`.batch_code and mm_batch.`material_id` = mm_profile.`material_id`\n" +
                                "left join `mm_characteristic_value` on mm_profile.id = mm_characteristic_value.`reference_id`\n" +
                                "where mm_profile.material_id = :materialId and mm_profile.batch_code = :batchCode",
                        Params.builder(2).pv("materialId", materialId).pv("batchCode", split[1]).build())
                                .ifPresent(map -> {
                                    item.setBatchCode(split[1]);
                                    item.setBatchId((Long) map.get("id"));
                                    item.setColor((String) map.get("value"));
                                });
            }

            item.setQuantity(new BigDecimal(quantityArr[i]));

            Material material = idMaterialMap.get(item.getMaterialId());
            item.setMaterialCode(material.getCode());
            item.setMaterialText(material.getName() + " " + material.getSpecificationText());
            item.setUnit(material.getBaseUnit());
            item.setUnitText(dictService.getDictByTypeAndName("unit", material.getBaseUnit()).get().getLabel());
            item.setUnitPrice(material.getStandardPrice());

            itemList.add(item);
        }

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
    public static class GoodsReceiptItem {

        private String produceOrderCode;

        private Long materialId;

        private String materialCode;

        private String materialText;

        private String unitText;

        private String color;

        private BigDecimal quantity;

        private BigDecimal goodsReceiptQuantity;

        private BigDecimal openQuantity;

        public Long getId() {
            return materialId;
        }

        public Boolean getComplete() {
            return BigDecimalUtils.eq(openQuantity, BigDecimal.ZERO);
        }

    }
}

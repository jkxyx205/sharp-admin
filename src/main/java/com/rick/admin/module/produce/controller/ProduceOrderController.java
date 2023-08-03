package com.rick.admin.module.produce.controller;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.ProduceOrderService;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    ProduceOrderService produceOrderService;

    MaterialDAO materialDAO;

    DictService dictService;

    SharpService sharpService;

    MaterialService materialService;

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

            Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(produceOrder.getItemList().stream().map(ProduceOrder.Item::getMaterialId).collect(Collectors.toSet()));
            materialService.fillMaterialDescription(produceOrder.getItemList());

            model.addAttribute("po", produceOrder);

            // 领料记录
            model.addAttribute("goodsReceiptItemList", getGoodsReceiptItemList(produceOrder.getCode()));
        } else {
            model.addAttribute("po", new ProduceOrder());
        }

        return "modules/produce_order";
    }

    private List<ProduceOrderController.GoodsReceiptItem> getGoodsReceiptItemList(String produceOrderCode) {
        String sql = "select '${produceOrderCode}' produceOrderCode, t1.material_id, mm_material.code materialCode, mm_material.base_unit unitText, t1.quantity, IFNULL(t2.quantity, 0) goodsReceiptQuantity, (t1.quantity - IFNULL(t2.quantity, 0)) openQuantity  from (\n" +
                "select produce_bom_detail.`material_id`, sum(produce_bom_detail.quantity * produce_order_item.quantity) quantity from produce_order \n" +
                "inner join produce_order_item on produce_order_item.`produce_order_id` = produce_order.id \n" +
                "inner join produce_bom on produce_bom.`material_id` = produce_order_item.material_id\n" +
                "inner join produce_bom_detail on produce_bom.id = produce_bom_detail.`bom_id`\n" +
                "inner join mm_material on mm_material.id = produce_bom_detail.`material_id`\n" +
                "where produce_order.code = :produceOrderCode group by material_id) t1 \n" +
                "left join (select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :produceOrderCode group by root_reference_item_id)t2 on t1.material_id = t2.root_reference_item_id\n" +
                "left join `mm_material` on mm_material.id = t1.material_id";

        List<GoodsReceiptItem> goodsReceiptItemList = sharpService.query(sql, Params.builder(1).pv("produceOrderCode", produceOrderCode).build(), GoodsReceiptItem.class);
        if (CollectionUtils.isNotEmpty(goodsReceiptItemList)) {
            Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(goodsReceiptItemList.stream().map(ProduceOrderController.GoodsReceiptItem::getMaterialId).collect(Collectors.toSet()));
            for (GoodsReceiptItem item : goodsReceiptItemList) {
                Material material = idMaterialMap.get(item.getMaterialId());
                item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
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

//    @GetMapping("purchase_order")
//    public String a(Model model, String materialIds, String quantity) {
//        PurchaseOrder purchaseOrder = new PurchaseOrder();
//        purchaseOrder.setCreateBy(UserContextHolder.get().getId());
//
//        Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(materialIds);
//
//        List<PurchaseOrder.Item> itemList = new ArrayList<>();
//
//        String[] quantityArr = quantity.split(",");
//        String[] materialIdArr = materialIds.split(",");
//
//        for (int i = 0; i < materialIdArr.length; i++) {
//            PurchaseOrder.Item item = new PurchaseOrder.Item();
//            item.setMaterialId(Long.parseLong(materialIdArr[i]));
//            item.setQuantity(new BigDecimal(quantityArr[i]));
//
//            Material material = idMaterialMap.get(item.getMaterialId());
//            item.setMaterialCode(material.getCode());
//            item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
//            item.setUnit(material.getBaseUnit());
//            item.setUnitText(dictService.getDictByTypeAndName("unit", material.getBaseUnit()).get().getLabel());
//            item.setUnitPrice(material.getStandardPrice());
//
//            itemList.add(item);
//        }
//
//        purchaseOrder.setItemList(itemList);
//
//        model.addAttribute("po", purchaseOrder);
//
//        model.addAttribute("createName", dictService.getDictByTypeAndName("sys_user", purchaseOrder.getCreateBy().toString()).get().getLabel());
//        model.addAttribute("createTime", Time2StringUtils.format(purchaseOrder.getCreateTime()));
//
//        return "modules/purchase/purchase_order";
//    }

    @GetMapping("purchase_order")
    public String batch(Model model, String materialIds, String quantity) {
        Map<Long, Material> idMaterialMap = materialDAO.selectByIdsAsMap(materialIds);

        List<PurchaseOrder.Item> itemList = new ArrayList<>();

        String[] quantityArr = quantity.split(",");
        String[] materialIdArr = materialIds.split(",");

        for (int i = 0; i < materialIdArr.length; i++) {
            PurchaseOrder.Item item = new PurchaseOrder.Item();
            item.setMaterialId(Long.parseLong(materialIdArr[i]));
            item.setQuantity(new BigDecimal(quantityArr[i]));

            Material material = idMaterialMap.get(item.getMaterialId());
            item.setMaterialCode(material.getCode());
            item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
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

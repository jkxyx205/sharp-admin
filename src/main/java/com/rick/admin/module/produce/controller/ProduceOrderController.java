package com.rick.admin.module.produce.controller;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Material;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.ProduceOrderService;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    EntityDAO<ProduceOrder, Long> produceOrderDAO;

    ProduceOrderService purchaseOrderService;

    MaterialDAO materialDAO;

    DictService dictService;

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

            for (ProduceOrder.Item item : produceOrder.getItemList()) {
                Material material = idMaterialMap.get(item.getMaterialId());
                item.setMaterialCode(material.getCode());
                item.setMaterialText(material.getName() + " " + material.getCharacteristicText());
                item.setUnitText(dictService.getDictByTypeAndName("unit", item.getUnit()).get().getLabel());
            }

            model.addAttribute("po", produceOrder);
        } else {
            model.addAttribute("po", new ProduceOrder());
        }

        return "modules/produce_order";
    }

    /**
     * 修改或更新
     * @param produceOrder
     * @return
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public ProduceOrder saveOrUpdate(@RequestBody ProduceOrder produceOrder) {
        purchaseOrderService.saveOrUpdate(produceOrder);
        return produceOrder;
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

}

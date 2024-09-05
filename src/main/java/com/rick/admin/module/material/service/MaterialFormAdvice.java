package com.rick.admin.module.material.service;

import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.entity.Material;
import com.rick.common.http.exception.BizException;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.expression.Numbers;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Rick.Xu
 * @date 2023/6/2 11:40
 */
@Component("materialFormAdvice")
@RequiredArgsConstructor
public class MaterialFormAdvice implements FormAdvice {

    private final DictService dictService;

    private final StockDAO stockDAO;

    private final MaterialService materialService;

    private final MaterialDAO materialDAO;

    private final SharpService sharpService;


    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        // code处理
        String code = (String) values.get("code");
        String materialType = (String) values.get("materialType");
        if (StringUtils.isBlank(code)) {
            Optional<String> remarkOptional = sharpService.queryForObject("select remark from sys_dict where type = :type AND name = :name",
                    Params.builder(2).pv("type", "MATERIAL_TYPE").pv("name", materialType)
                            .build(),
                    String.class);


            Long newNumber;
            String newCode;
            Long currentNumber = Long.parseLong(remarkOptional.get());
            while (true) {
                newNumber = ++currentNumber;
                newCode = materialType.charAt(0) + String.format("%05d", newNumber);
                try {
                    materialDAO.assertCodeNotExists(newCode);
                    values.put("code", newCode);
                    break;
                } catch (Exception e) {}
            }

            sharpService.getNamedJdbcTemplate().update("update sys_dict set remark = :newNumber where type = :type AND name = :name",
                    Params.builder(2).pv("type", "MATERIAL_TYPE").pv("name", materialType).pv("newNumber", newNumber)
                            .build());
        }

        // checkbox List => boolean
        if (CollectionUtils.isEmpty((Collection<?>) values.get("batchManagement"))) {
            values.put("batchManagement", false);
        } else {
            values.put("batchManagement", true);
        }

        if (CollectionUtils.isEmpty((Collection<?>) values.get("serialManagement"))) {
            values.put("serialManagement", false);
        } else {
            values.put("serialManagement", true);
        }

        HashMap<String, String> mrpMap = (HashMap<String, String>) values.get("mrp");
        if (Objects.nonNull(mrpMap)) {
            String safetyStockQuantity = mrpMap.get("safetyStockQuantity");
            String maximumStockQuantity = mrpMap.get("maximumStockQuantity");
            values.put("mrp", new Material.Mrp(StringUtils.isNotEmpty(safetyStockQuantity) ? new BigDecimal(safetyStockQuantity) : null,
                    StringUtils.isNotEmpty(maximumStockQuantity) ? new BigDecimal(maximumStockQuantity) : null));
        }

        // 显示特征分类
        String classification = (String) values.get("classification");
        if (StringUtils.isNotBlank(classification)) {
            values.put("classificationList", Arrays.asList(
                    Classification.builder()
                            .classificationCode(classification)
                            .build()));
        }

    }

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {

    }

    @Override
    public void beforeGetInstance(Long instanceId, Map<String, Object> valueMap) {
        Numbers numbers = new Numbers(Locale.CHINA);

        BigDecimal stockQuantity = stockDAO.getStockQuantityByMaterialId((Long)valueMap.get("id"), 719893335619162112L);
        valueMap.put("stockQuantity", stockQuantity + " " + dictService.getDictByTypeAndName("unit", valueMap.get("baseUnit").toString()).get().getLabel());
        BigDecimal standardPrice = (BigDecimal) valueMap.get("standardPrice");

        if (Objects.nonNull(standardPrice)) {
            valueMap.put("stockQuantityPrice", numbers.formatDecimal(stockQuantity.multiply(standardPrice), 1, "COMMA", 2, "POINT") + " 元");
        }

        // 显示特征分类
        List<Classification> classificationList = (List<Classification>) valueMap.get("classificationList");
        if (CollectionUtils.isNotEmpty(classificationList)) {
            valueMap.put("classification", classificationList.get(0).getClassificationCode());
        }
    }

    @Override
    public boolean insertOrUpdate(Map<String, Object> values) {
        Material material = materialDAO.mapToEntity(values);

        // 检查是否已经存在物料的描述
        boolean isExists = materialService.checkMaterialIfExists(material.getId(), material.getName(), material.getSpecificationList());

        if (isExists) {
            throw new BizException(50012, "物料已经存在！请查询确认一下！");
        }


//        if (Objects.equals(material.getBatchManagement(), Boolean.TRUE)) {
//            // 批次物料加上颜色特征
//            material.setClassificationList(Arrays.asList(
//                    Classification.builder()
//                            .classificationCode("COLOR")
//                            .characteristicValueList(Arrays.asList(
//                                    CharacteristicValue.builder()
//                                            .characteristicCode("COLOR")
//                                            .build()
//                            ))
//                            .build()));
//        }


        materialService.saveOrUpdate(material);
        return true;
    }

    @Override
    public void beforeDeleteInstance(Long instanceId) {
        // 检查是否允许删除
        Optional<Long> optional = sharpService.queryForObject("select sum(c) from (\n" +
                        "select count(*) c from inv_stock where material_id = :materialId\n" +
                        "union all\n" +
                        "select count(*) c from `produce_order_item_detail` where material_id = :materialId\n" +
                        "union all\n" +
                        "select count(*) c from `pur_purchase_order_item` where material_id = :materialId\n" +
                        ") count",
                Params.builder(1).pv("materialId", instanceId).build()
                , Long.class);
        if (optional.get().longValue() > 0) {
            throw new BizException(500, "该物料已经被使用，不能被删除!");
        }
    }
}

package com.rick.admin.module.inventory.dao;

import com.rick.admin.module.inventory.entity.Stock;
import com.rick.admin.module.material.model.MaterialIdBatchCode;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.db.service.support.Params;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 12:22
 */
@Repository
public class StockDAO extends EntityDAOImpl<Stock, Long> {

    public Optional<Stock> getStockQuantity(Long plantId, Long materialId, Long batchId) {
        List<Stock> values = selectByParams(Params.builder(3).pv("plantId", plantId).pv("materialId", materialId).pv("batchId", batchId).build(),
                "plant_id = :plantId AND material_id = :materialId AND batch_id = :batchId");
        return expectedAsOptional(values);
    }

    public BigDecimal getStockQuantityByMaterialId(Long materialId) {
        List<BigDecimal> values = sharpService.query("select IFNULL(sum(quantity), 0) from inv_stock where material_id = :materialId",
                Params.builder(1).pv("materialId", materialId).build(), BigDecimal.class);
        return expectedAsOptional(values).orElse(BigDecimal.ZERO);
    }

//    public Map<Long, BigDecimal> getStockQuantityByMaterialId(Collection<?> materialIds) {
//        return getStockQuantityByMaterialId(materialIds, null);
//    }

//    public Map<Long, BigDecimal> getStockQuantityByMaterialId(Collection<?> materialIds, Long plantId) {
//        List<Stock> values = sharpService.query("select material_id materialId, IFNULL(sum(quantity), 0) quantity from inv_stock where material_id IN (:materialId) AND plant_id = :plantId group by material_id",
//                Params.builder(2).pv("materialIds", materialIds).pv("plantId", plantId).build(), Stock.class);
//        return values.stream().collect(Collectors.toMap(s -> s.getMaterialId(), s -> s.getQuantity()));
//    }

    public Map<MaterialIdBatchCode, BigDecimal> getStockQuantityByMaterialIdAndBatchCode(Long plantId, Collection<MaterialIdBatchCode> materialIdBatchCodes) {
        List<Map<String, Object>> list = sharpService.query(" select material_id, batch_code, sum(quantity) quantity from inv_stock \n" +
                "where concat(material_id, IFNULL(batch_code, '')) IN (:keyIds) AND plant_id = :plantId\n" +
                " group by material_id, batch_id", Params.builder(2).pv("plantId", plantId)
                .pv("keyIds", materialIdBatchCodes.stream().map(mb -> mb.getMaterialId() + Objects.toString(mb.getBatchCode(), "")).collect(Collectors.toSet()))
                .build());

        return list.stream().collect(Collectors.toMap(m -> new MaterialIdBatchCode((Long) m.get("material_id"), Objects.toString(m.get("batch_code"), "")), m -> (BigDecimal) m.get("quantity")));
    }

}
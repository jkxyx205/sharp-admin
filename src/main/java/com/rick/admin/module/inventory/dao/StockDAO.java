package com.rick.admin.module.inventory.dao;

import com.rick.admin.module.inventory.entity.Stock;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.db.service.support.Params;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 12:22
 */
@Repository
public class StockDAO extends EntityDAOImpl<Stock, Long> {

    public Optional<Stock> getStockQuantity(Long plantId, Long materialId) {
        List<Stock> values = selectByParams(Params.builder(2).pv("plantId", plantId).pv("materialId", materialId).build(), "plant_id = :plantId AND material_id = :materialId");
        return expectedAsOptional(values);
    }

    public BigDecimal getStockQuantityByMaterialId(Long materialId) {
        List<BigDecimal> values = sharpService.query("select IFNULL(sum(quantity), 0) from inv_stock where material_id = :materialId",
                Params.builder(1).pv("materialId", materialId).build(), BigDecimal.class);
        return expectedAsOptional(values).orElse(BigDecimal.ZERO);
    }

    public Map<Long, BigDecimal> getStockQuantityByMaterialId(Collection<?> materialIds) {
        List<Stock> values = sharpService.query("select material_id materialId, IFNULL(sum(quantity), 0) quantity from inv_stock where material_id IN (:materialId) group by material_id",
                Params.builder(1).pv("materialIds", materialIds).build(), Stock.class);
        return values.stream().collect(Collectors.toMap(s -> s.getMaterialId(), s -> s.getQuantity()));
    }
}
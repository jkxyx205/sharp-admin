package com.rick.admin.module.inventory.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.admin.module.inventory.entity.Stock;
import com.rick.admin.module.material.dao.MaterialDAO;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.CharacteristicHelper;
import com.rick.admin.module.material.service.ClassificationService;
import com.rick.common.http.exception.BizException;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;

import static com.rick.admin.common.exception.ExceptionCodeEnum.LOW_STOCKS_ERROR;

/**
 * @author Rick.Xu
 * @date 2023/6/11 12:23
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class StockService {

    StockDAO stockDAO;

    MaterialDAO materialDAO;

    BatchService batchService;

    ClassificationService classificationService;

    /**
     * 修改库存
     * @param stock
     */
    public void changeStockQuantity(Stock stock) {
        changeStockQuantity(stock, false);
    }

    public void changeStockQuantity(Stock stock, boolean allowNegativeStock) {
        Optional<Stock> optional = stockDAO.getStockQuantity(stock.getPlantId(), stock.getMaterialId(), stock.getBatchId());

        if (optional.isPresent()) {
            Stock stockInDb = optional.get();
            stock.setId(stockInDb.getId());
            stock.setQuantity(stock.getQuantity().add(stockInDb.getQuantity()));

            if (BigDecimalUtils.eq(stock.getQuantity(), BigDecimal.ZERO)) {
                stockDAO.deleteById(stock.getId());
                return;
            }
        }

        if (BigDecimalUtils.lt(stock.getQuantity(), BigDecimal.ZERO)) {
            if (allowNegativeStock) {
                stock.setQuantity(BigDecimal.ZERO);
                if (stock.getId() != null) {
                    stockDAO.deleteById(stock.getId());
                }
                return;
            } else {
                throw new BizException(LOW_STOCKS_ERROR,
                        new Object[]{materialDAO.selectSingleValueById(stock.getMaterialId(), "code", String.class).get()});
            }
        }

        stockDAO.insertOrUpdate(stock);
    }

    /**
     * 基于库房物料 进行盘点
     * @param plantId
     * @param materialIds
     * @return
     */
    public List<Stock> findAll(Long plantId, Collection<Long> materialIds) {
//        List<Stock> stockList = stockDAO.selectByParams(Params.builder(2).build(), "plant_id = 719893335619162112 and material_id = 731499486144483329 and id = 766629121320407042");
        List<Stock> stockList = stockDAO.selectByParams(Params.builder(2).pv("plantId", plantId).pv("materialIds", materialIds).build(), "plant_id = :plantId and material_id IN (:materialIds)");
        Map<Long, List<Classification>> materialIdClassificationMap = classificationService.findAllAsMap();
        for (Stock stock : stockList) {
            stock.setId(null);
            stock.setQuantity(null);
            List<Classification> classificationList = materialIdClassificationMap.get(stock.getMaterialId());
            stock.setClassificationList(CollectionUtils.isEmpty(classificationList) ? Collections.emptyList() : classificationList);
        }

        CharacteristicHelper.handlerSameReference(stockList);
        batchService.fillCharacteristicValue(stockList);
        return stockList;
    }

}
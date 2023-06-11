package com.rick.admin.module.inventory.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.inventory.dao.StockDAO;
import com.rick.admin.module.inventory.entity.Stock;
import com.rick.common.http.exception.BizException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Optional;

import static com.rick.admin.common.ExceptionCodeEnum.LOW_STOCKS_ERROR;

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

    /**
     * 修改库存
     * @param stock
     */
    public void changeStockQuantity(Stock stock) {
        Optional<Stock> optional = stockDAO.getStockQuantity(stock.getPlantId(), stock.getMaterialId());

        if (optional.isPresent()) {
            Stock stockInDb = optional.get();
            stock.setId(stockInDb.getId());
            stock.setQuantity(stock.getQuantity().add(stockInDb.getQuantity()));

            if (BigDecimalUtils.eq(stock.getQuantity(), BigDecimal.ZERO)) {
                stockDAO.deleteById(stock.getId());
                return;
            }

        }

        // check
        if (BigDecimalUtils.lt(stock.getQuantity(), BigDecimal.ZERO)) {
            throw new BizException(LOW_STOCKS_ERROR);
        }

        stockDAO.insertOrUpdate(stock);
    }



}
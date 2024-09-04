package com.rick.admin.module.purchase.service;

import com.google.common.collect.Sets;
import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchSupport;
import com.rick.admin.module.purchase.entity.LatestPrice;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.support.Params;
import com.rick.db.util.OptionalUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2024/6/23 15:24
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class LatestPriceService {

    EntityDAO<LatestPrice, Long> latestPriceDAO;

    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static Map<Long, String> priceCharacteristicMapping = new HashMap<>();

    static {
        priceCharacteristicMapping.put(725448238349443072L, "TIEX"); // 多个特征值用空格隔开
    }

    /**
     * 批量更新最新价格
     * @param latestPrices
     */
    public void updatePrice(Collection<LatestPrice> latestPrices) {
        if (CollectionUtils.isEmpty(latestPrices)) {
            return;
        }

        Set<String> keys = latestPrices.stream().map(latestPrice -> latestPrice.getMaterialId() + (Objects.isNull(latestPrice.getBatchCode()) ? "" : "" + latestPrice.getBatchCode()) + latestPrice.getPartnerId())
                .collect(Collectors.toSet());

        namedParameterJdbcTemplate.update("DELETE FROM pur_latest_price WHERE concat(material_id, IFNULL(batch_code, ''), partner_id) IN (:keys)",
                Params.builder(1).pv("keys", keys).build());

        latestPriceDAO.insert(latestPrices);
    }

    public String priceBatchCode(Long materialId, String batchCode, List<Classification> classificationList) {
        if (priceCharacteristicMapping.keySet().contains(materialId)) { // 部分特征值控制价格
            String characteristicString = priceCharacteristicMapping.get(materialId);
            Set<String> characteristics = Sets.newHashSet(characteristicString.split("\\s+"));

            Iterator<Classification> iterator = classificationList.iterator();
            while (iterator.hasNext()) {
                Classification classification = iterator.next();
                Iterator<CharacteristicValue> iterator1 = classification.getCharacteristicValueList().iterator();
                while (iterator1.hasNext()) {
                    CharacteristicValue characteristicValue = iterator1.next();
                    if (!characteristics.contains(characteristicValue.getCharacteristicCode())) {
                        iterator1.remove();
                    }
                }
            }

            return BatchSupport.characteristicToCode(classificationList);
        }

        return batchCode;
    }

    /**
     * 获取供应商物料的最新价格
     * @param materialId
     * @param batchCode
     * @param partnerId
     * @return
     */
    public Optional<BigDecimal> getLatestPrice(Long materialId, String batchCode, Long partnerId) {
        Assert.notNull(materialId, "materialId can not be null");
        Assert.notNull(partnerId, "partnerId can not be null");

        Optional<LatestPrice> latestPriceOptional = OptionalUtils.expectedAsOptional(latestPriceDAO.selectByParams(Params.builder(3)
                        .pv("materialId", materialId)
                        .pv("batchCode", batchCode)
                        .pv("partnerId", partnerId)
                        .build(),
                "price",
                "material_id = :materialId AND batch_code = :batchCode AND partner_id = :partnerId"));

        return latestPriceOptional.isPresent() ? Optional.of(latestPriceOptional.get().getPrice()) : Optional.empty();
    }

    /**
     * 获取物料的最新价格
     * @return
     */
    public Map<String, BigDecimal> getMaterialPriceMap() {
        return latestPriceDAO.getSharpService()
                .queryForKeyValue("select distinct concat(material_id, ifnull(batch_code, '')), price from pur_latest_price order by update_time", null);

    }

}
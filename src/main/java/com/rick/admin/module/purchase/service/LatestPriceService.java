package com.rick.admin.module.purchase.service;

import com.rick.admin.module.purchase.entity.LatestPrice;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
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

    public void updatePrice(Collection<LatestPrice> latestPrices) {
        if (CollectionUtils.isEmpty(latestPrices)) {
            return;
        }

        Set<String> keys = latestPrices.stream().map(latestPrice -> latestPrice.getMaterialId() + (Objects.isNull(latestPrice.getBatchId()) ? "" : "" + latestPrice.getBatchId()) + latestPrice.getPartnerId())
                .collect(Collectors.toSet());

        namedParameterJdbcTemplate.update("DELETE FROM pur_latest_price WHERE concat(material_id, IFNULL(batch_id, ''), partner_id) IN (:keys)",
                Params.builder(1).pv("keys", keys).build());

        latestPriceDAO.insert(latestPrices);
    }
}
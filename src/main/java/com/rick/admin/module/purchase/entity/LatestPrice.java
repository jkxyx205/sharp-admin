package com.rick.admin.module.purchase.entity;

import com.google.common.base.Objects;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * @author Rick.Xu
 * @date 2024/6/23 15:19
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "pur_latest_price", comment = "物料最新价格")
public class LatestPrice extends BaseEntity<Long> {

    Long materialId;

    String materialCode;

    Long batchId;

    String batchCode;

    Long partnerId;

    BigDecimal price;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LatestPrice that = (LatestPrice) o;
        return Objects.equal(materialId, that.materialId) && Objects.equal(batchId, that.batchId) && Objects.equal(partnerId, that.partnerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), materialId, batchId, partnerId);
    }
}
package com.rick.admin.module.material.entity;

import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Embedded;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/2 10:33
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "mm_material", comment = "物料")
public class Material extends BaseCodeEntity {

    String materialType;

    String name;

    String remark;

    String baseUnit;

    BigDecimal standardPrice;

    Long categoryId;

    Boolean batchManagement;

    Boolean serialManagement;

    Integer grossWeight;

    Integer netWeight;

    String weightUnit;

    String volume;

    String volumeUnit;

    String size;

    @Column(comment = "规格", columnDefinition = "text", value = "characteristic")
    private List<List<String>> characteristicList;

    @Embedded
    private Mrp mrp;

    @Column(comment = "附件", columnDefinition = "text", value = "attachment", nullable = false)
    private List<Map<String, Object>> attachmentList;

    public String getCharacteristicText() {
        return CollectionUtils.isNotEmpty(getCharacteristicList()) ? getCharacteristicList().stream().map(list -> list.get(1).toString()).collect(Collectors.joining("/")) : "";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Mrp {

        BigDecimal safetyStockQuantity;

        BigDecimal maximumStockQuantity;
    }
}
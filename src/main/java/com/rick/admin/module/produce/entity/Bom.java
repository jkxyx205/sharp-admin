package com.rick.admin.module.produce.entity;

import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/6/30 15:38
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "produce_bom", comment = "物料BOM表")
public class Bom extends BaseEntity {

    @NotNull
    @Column(comment = "物料")
    Long materialId;

    @Column(comment = "BOM 模版")
    Long bomTemplateId;

    String remark;

    @Valid
    @NotEmpty
    @OneToMany(subTable = "produce_bom_detail", reversePropertyName = "bomId", cascadeInsertOrUpdate = true, joinValue = "bom_id")
    List<Item> itemList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "produce_bom_detail", comment = "物料BOM表详情")
    public static class Item extends BaseEntity implements MaterialDescription {

        @NotNull
        @Column(comment = "物料")
        Long materialId;

        @NotNull
        BigDecimal quantity;

        @NotNull
        String unit;

        String remark;

        Long componentDetailId;

        Long bomId;

//        @Column(comment = "含税单价")
        @Transient
        BigDecimal unitPrice;

        @Transient
        String materialCode;

        @Transient
        String materialText;

        @Transient
        String unitText;

        public BigDecimal getAmount() {
            if (Objects.nonNull(unitPrice)) {
                return unitPrice.multiply(quantity);
            }

            return null;
        }

    }

}
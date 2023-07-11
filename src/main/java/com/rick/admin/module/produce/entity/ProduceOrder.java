package com.rick.admin.module.produce.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/7/11 11:28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "produce_order", comment = "生产单")
public class ProduceOrder extends BaseCodeEntity {

    StatusEnum status;

    @OneToMany(subTable = "produce_order_item", reversePropertyName = "produceOrderId", cascadeInsertOrUpdate = true, joinValue = "produce_order_id")
    List<Item> itemList;

    String remark;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "produce_order_item", comment = "生产单行项目")
    public static class Item extends BaseEntity {

        @NotNull
        @Column(comment = "物料")
        Long materialId;

        @NotNull
        @Column(comment = "数量")
        BigDecimal quantity;

        @NotNull
        @Column(comment = "单位")
        String unit;


        @Column(comment = "备注")
        String remark;

        Long produceOrderId;

        @Column(updatable = false)
        String produceOrderCode;

        @Transient
        String materialCode;

        @Transient
        String materialText;

        @Transient
        String unitText;
    }

    @AllArgsConstructor
    @Getter
    public enum StatusEnum {
        PLANNING("计划中"),
        PROCESSING("完成领料"),
        DONE("生产完成");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static ProduceOrder.StatusEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }
}
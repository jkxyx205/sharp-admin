package com.rick.admin.module.purchase.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.admin.module.material.service.MaterialDescriptionHandler;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/6/19 15:52
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "pur_purchase_order", comment = "采购订单")
public class PurchaseOrder extends BaseCodeEntity {

    @NotNull
    @Column(comment = "供应商")
    Long partnerId;

//    @NotBlank
    @Column(comment = "联系人")
    String contactPerson;

//    @NotBlank
    @Column(comment = "联系方式")
    String contactNumber;

//    @NotBlank
    @Column(comment = "联系邮箱")
    String contactMail;

    @NotNull
    @Column(comment = "库房")
    Long plantId;

    @Column(comment = "备注")
    String remark;

    StatusEnum status;

    @NotEmpty
    @OneToMany(subTable = "pur_purchase_order_item", reversePropertyName = "purchaseOrderId", cascadeInsertOrUpdate = true, joinValue = "purchase_order_id")
    List<Item> itemList;

    public BigDecimal getAmount() {
        return itemList.stream().map(PurchaseOrder.Item::getAmount).reduce(BigDecimal.ZERO, (a1, a2) -> a1.add(a2));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "pur_purchase_order_item", comment = "采购订单行项目")
    public static class Item extends BaseEntity implements MaterialDescriptionHandler {

        @NotNull
        @Column(comment = "物料")
        Long materialId;

        @NotNull
        @Column(comment = "物料Code")
        String materialCode;

        @NotNull
        @Column(comment = "数量")
        BigDecimal quantity;

        @NotNull
        @Column(comment = "单位")
        String unit;

        @NotNull
        @Column(comment = "含税单价")
        BigDecimal unitPrice;

        @NotNull
        @Column(comment = "交货日期")
        LocalDate deliveryDate;

        @Column(comment = "备注")
        String remark;

        @Column(value = "is_complete", comment = "完成")
        Boolean complete;

        Long purchaseOrderId;

        @Column(updatable = false)
        String purchaseOrderCode;

        Long batchId;

        String batchCode;

        String color;

        @Transient
        MaterialDescription materialDescription;

        public BigDecimal getAmount() {
            if (Objects.nonNull(unitPrice)) {
                return unitPrice.multiply(quantity);
            }

            return null;
        }

    }

    @AllArgsConstructor
    @Getter
    public enum StatusEnum {
        PLANNING("采购中"),
        DONE("采购完成");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static PurchaseOrder.StatusEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }
}
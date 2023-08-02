package com.rick.admin.module.purchase.entity;

import com.rick.admin.module.material.service.MaterialDescription;
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

    @NotNull
    @Column(comment = "库房")
    Long plantId;

    @NotNull
    @Column(comment = "交货日期")
    LocalDate deliveryDate;

    @Column(comment = "备注")
    String remark;

    @NotEmpty
    @OneToMany(subTable = "pur_purchase_order_item", reversePropertyName = "purchaseOrderId", cascadeInsertOrUpdate = true, joinValue = "purchase_order_id")
    List<Item> itemList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "pur_purchase_order_item", comment = "采购订单行项目")
    public static class Item extends BaseEntity implements MaterialDescription {

        @NotNull
        @Column(comment = "物料")
        Long materialId;

        @NotNull
        @Column(comment = "数量")
        BigDecimal quantity;

        @NotNull
        @Column(comment = "单位")
        String unit;

        @NotNull
        @Column(comment = "含税单价")
        BigDecimal unitPrice;

        @Column(comment = "备注")
        String remark;

        @Column(value = "is_complete", comment = "完成", updatable = false)
        Boolean complete;

        Long purchaseOrderId;

        @Column(updatable = false)
        String purchaseOrderCode;

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
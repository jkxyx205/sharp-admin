package com.rick.admin.module.purchase.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.admin.module.core.entity.ContactInfo;
import com.rick.admin.module.core.model.ReferenceTypeEnum;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchHandler;
import com.rick.admin.module.material.service.CharacteristicHelper;
import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.admin.module.material.service.MaterialDescriptionHandler;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
public class PurchaseOrder extends BaseCodeEntity<Long> {

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

    @Column(comment = "附件", columnDefinition = "text", value = "attachment", nullable = false)
    private List<Map<String, Object>> attachmentList;

    @NotEmpty
    @OneToMany(subTable = "pur_purchase_order_item", reversePropertyName = "purchaseOrderId", cascadeInsertOrUpdate = true, joinValue = "purchase_order_id")
    List<Item> itemList;

    public BigDecimal getAmount() {
        return itemList.stream().map(PurchaseOrder.Item::getAmount).reduce(BigDecimal.ZERO, (a1, a2) -> a1.add(a2));
    }

    public void setItemList(List<Item> itemList) {
        CharacteristicHelper.handlerSameReference(itemList);
        this.itemList = itemList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "pur_purchase_order_item", comment = "采购订单行项目")
    public static class Item extends BaseEntity<Long> implements MaterialDescriptionHandler, BatchHandler {

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

        @Column(value = "is_purchase_send", comment = "采购直发")
        Boolean purchaseSend;

        Long batchId;

        String batchCode;

        ReferenceTypeEnum referenceType1;

        @Column(updatable = false)
        Long referenceId1;

        ReferenceTypeEnum referenceType2;

        @Column(updatable = false)
        Long referenceId2;

        @Column(updatable = false)
        Long produceOrderId;

        @OneToMany(oneToOne = true, subTable = "core_contact", joinValue = "instance_id", cascadeInsertOrUpdate = true, reversePropertyName = "instanceId")
        private ContactInfo contactInfo;

        /**
         * 特征值
         */
        @Transient
        @Select(table = "mm_classification", joinValue = "material_id", referencePropertyName = "materialId")
        private List<Classification> classificationList;

        @Transient
        MaterialDescription materialDescription;

        @Transient
        @Sql(value = "select source_order_num sourceOrderNum, produce_order.id,\n" +
                "                       produce_order.code,\n" +
                "                       produce_order.partner_id,\n" +
                "                       core_partner.name, core_partner.short_name from produce_order, core_partner where core_partner.id = produce_order.partner_id\n" +
                "                       AND produce_order.id = :produceOrderId", params = "produceOrderId@produceOrderId", nullWhenParamsIsNull = {"produceOrderId"})
        private Map<String, Object> soInfo;

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
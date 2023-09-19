package com.rick.admin.module.produce.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
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

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @NotNull
    @Column(comment = "客户")
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

    @Column(comment = "客户订单号")
    String sourceOrderNum;

    @Column(comment = "下单日期")
    LocalDate orderDate;

    StatusEnum status;

    String remark;

    @OneToMany(subTable = "produce_order_item", reversePropertyName = "produceOrderId", cascadeInsertOrUpdate = true, joinValue = "produce_order_id")
    List<Item> itemList;

    @Column(comment = "附件", columnDefinition = "text", value = "attachment", nullable = false)
    private List<Map<String, Object>> attachmentList;

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
    @Table(value = "produce_order_item", comment = "生产单行项目")
    public static class Item extends BaseEntity implements MaterialDescriptionHandler, BatchHandler {

        @NotNull
        @Column(comment = "物料")
        Long materialId;

        @NotNull
        @Column(comment = "物料Code")
        String materialCode;

        @Column(comment = "规格")
        String specification;

        @NotNull
        @Column(comment = "数量")
        BigDecimal quantity;

        @NotNull
        @Column(comment = "单位")
        String unit;

//        @NotNull
        @Column(comment = "含税单价")
        BigDecimal unitPrice;

        @NotNull
        @Column(comment = "交货日期")
        LocalDate deliveryDate;

        @Column(comment = "客户物料编号")
        String customerMaterialCode;

        @Column(comment = "备注")
        String remark;

        Long produceOrderId;

        Long batchId;

        String batchCode;

        /**
         * 特征值
         */
        @Transient
        @Select(table = "mm_classification", joinValue = "material_id", referencePropertyName = "materialId")
        private List<Classification> classificationList;

        @Column(value = "is_complete", comment = "完成发货")
        Boolean complete;

        @Column(updatable = false)
        String produceOrderCode;

        @Transient
        MaterialDescription materialDescription;

//        @Valid
//        @NotEmpty
        // 原材料无BOM
        @OneToMany(subTable = "produce_order_item_detail", reversePropertyName = "produceOrderItemId", cascadeInsertOrUpdate = true, joinValue = "produce_order_item_id")
        List<Detail> itemList;

        public BigDecimal getAmount() {
            if (Objects.nonNull(unitPrice)) {
                return unitPrice.multiply(quantity);
            }

            return null;
        }

        public void setItemList(List<Detail> itemList) {
            CharacteristicHelper.handlerSameReference(itemList);
            this.itemList = itemList;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SuperBuilder
        @Table(value = "produce_order_item_detail", comment = "物料BOM实例详情")
        public static class Detail extends BaseEntity implements MaterialDescriptionHandler, BatchHandler {

            @NotNull
            @Column(comment = "物料")
            Long materialId;

            String materialCode;

            @NotNull
            BigDecimal quantity;

            @NotNull
            String unit;

            String remark;

            @NotNull
            Long componentDetailId;

            Long batchId;

            String batchCode;

            /**
             * 特征值
             */
            @Transient
            @Select(table = "mm_classification", joinValue = "material_id", referencePropertyName = "materialId")
            private List<Classification> classificationList;

            Long produceOrderItemId;

            @Column(value = "is_complete", comment = "完成")
            Boolean complete;

            @Transient
            @JsonProperty(access = JsonProperty.Access.READ_ONLY)
            MaterialDescription materialDescription;

        }
    }

    @AllArgsConstructor
    @Getter
    public enum StatusEnum {
        PLANNING("待审核"),
        PRODUCING("待生产"),
//        PROCESSING("完成领料"),
        PRODUCED("生产完成"),
        DONE("订单完成");

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
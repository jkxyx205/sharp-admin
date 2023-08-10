package com.rick.admin.module.produce.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
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


        @Column(comment = "备注")
        String remark;

        Long produceOrderId;

        String batchCode;

        String color;

        @Column(value = "is_complete", comment = "完成")
        Boolean complete;

        @Column(updatable = false)
        String produceOrderCode;

        @Transient
        MaterialDescription materialDescription;

        @Valid
        @NotEmpty
        @OneToMany(subTable = "produce_order_item_detail", reversePropertyName = "produceOrderItemId", cascadeInsertOrUpdate = true, joinValue = "produce_order_item_id")
        List<Detail> itemList;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @SuperBuilder
        @Table(value = "produce_order_item_detail", comment = "物料BOM实例详情")
        public static class Detail extends BaseEntity implements MaterialDescriptionHandler {

            @NotNull
            @Column(comment = "物料")
            Long materialId;

            @NotNull
            BigDecimal quantity;

            @NotNull
            String unit;

            String remark;

            @NotNull
            Long componentDetailId;

            String batchCode;

            String color;

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
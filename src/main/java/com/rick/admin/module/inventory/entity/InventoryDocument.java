package com.rick.admin.module.inventory.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/6 16:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "inv_document", comment = "物料凭证")
public class InventoryDocument extends BaseCodeEntity<Long> {

    @NotNull
    @Column(comment = "场景")
    TypeEnum type;

    @NotNull
    @Column(comment = "参考凭证类型", columnDefinition = "varchar(32)")
    ReferenceTypeEnum referenceType;

    @Column(comment = "参考凭证号")
    String referenceCode;

    @NotBlank
    @Column(comment = "root参考凭证号")
    String rootReferenceCode;

    @NotNull
    @Column(comment = "凭证日期")
    LocalDate documentDate;

    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull
    @Column(comment = "库房")
    Long plantId;

    @JsonSerialize(using = ToStringSerializer.class)
    @Column(comment = "操作人")
    Long operatorId;

    @Column(comment = "备注")
    String remark;

    @Column(comment = "是否已经被取消")
    Boolean canceled;

    @Column(comment = "附件", columnDefinition = "text", value = "attachment", nullable = false)
    private List<Map<String, Object>> attachmentList;

    @NotEmpty
    @OneToMany(subTable = "inv_document_item", reversePropertyName = "inventoryDocumentId", cascadeInsert = true, joinValue = "inventory_document_id")
    List<Item> itemList;

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
    @Table(value = "inv_document_item", comment = "物料凭证行项目")
    public static class Item extends BaseEntity<Long> implements MaterialDescriptionHandler, BatchHandler {

        @Column(comment = "场景")
        TypeEnum type;

        @NotNull
        @Column(comment = "参考凭证类型", columnDefinition = "varchar(32)")
        ReferenceTypeEnum referenceType;

        @Column(comment = "参考凭证号")
        String referenceCode;

        @JsonSerialize(using = ToStringSerializer.class)
        @Column(comment = "参考凭证行项目id")
        Long referenceItemId;

        @NotBlank
        @Column(comment = "root参考凭证号")
        String rootReferenceCode;

        @JsonSerialize(using = ToStringSerializer.class)
        @Column(comment = "root参考凭证行项目id")
        Long rootReferenceItemId;

        @NotNull
        @Column(comment = "移动类型")
        MovementTypeEnum movementType;

        @NotNull
        @Column(comment = "库房")
        @JsonSerialize(using = ToStringSerializer.class)
        Long plantId;

        @NotNull
        @Column(comment = "物料")
        @JsonSerialize(using = ToStringSerializer.class)
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

        Long batchId;

        String batchCode;

        Long inventoryDocumentId;

        String inventoryDocumentCode;

        @Transient
        MaterialDescription materialDescription;

        @Transient
        boolean allowNegativeStock = false;

        /**
         * BOM的的detailId
         */
        @Transient
        Long detailId;

        /**
         * 特征值
         */
        @Transient
        @Select(table = "mm_classification", joinValue = "material_id", referencePropertyName = "materialId")
        private List<Classification> classificationList;
    }

    @AllArgsConstructor
    @Getter
    public enum MovementTypeEnum {
        INBOUND("入库"),
        OUTBOUND("出库");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static MovementTypeEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

    @AllArgsConstructor
    @Getter
    public enum TypeEnum {
        INBOUND("入库"),
        OUTBOUND("出库"),
        RETURN("退货"),
        MOVING_TO_PRODUCE("领料"),
        RETURN_FROM_PRODUCE("退料"),
        CANCEL("取消"),
        DELETE("删除"),
        DISPLAY("显示"),
        COUNT("盘点"),
        CONSUME("用料");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static TypeEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

    @AllArgsConstructor
    @Getter
    public enum ReferenceTypeEnum {
        OTHER("无"),
        MATERIAL_DOCUMENT("物料凭证"),
        PO("采购订单"),
        PP("生产订单"),
        SO("销售订单");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static ReferenceTypeEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }
}
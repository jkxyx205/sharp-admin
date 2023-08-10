package com.rick.admin.module.inventory.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
public class InventoryDocument extends BaseCodeEntity {

    @NotNull
    @Column(comment = "场景")
    TypeEnum type;

    @NotNull
    @Column(comment = "参考凭证类型", columnDefinition = "varchar(32)")
    ReferenceTypeEnum referenceType;

    @Column(comment = "参考凭证号")
    String referenceCode;

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

    @NotEmpty
    @OneToMany(subTable = "inv_document_item", reversePropertyName = "inventoryDocumentId", cascadeInsert = true, joinValue = "inventory_document_id")
    List<Item> itemList;

    @Column(comment = "是否已经被取消")
    Boolean canceled;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "inv_document_item", comment = "物料凭证行项目")
    public static class Item extends BaseEntity implements MaterialDescriptionHandler {

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

        String color;

        Long inventoryDocumentId;

        String inventoryDocumentCode;

        @Transient
        MaterialDescription materialDescription;
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
        CANCEL("取消"),
        DISPLAY("显示"),
        COUNT("盘点");

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
        PDO("生产单");

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
package com.rick.admin.module.purchase.entity;

import com.rick.admin.module.core.model.ReferenceTypeEnum;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchHandler;
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

/**
 * @author Rick.Xu
 * @date 2023/9/25 16:37
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "pur_purchase_requisition", comment = "采购申请")
public class PurchaseRequisition extends BaseCodeEntity {

    @NotEmpty
    @OneToMany(subTable = "pur_purchase_requisition_item", reversePropertyName = "purchaseRequisitionId", cascadeInsertOrUpdate = true, joinValue = "purchase_requisition_id")
    List<PurchaseRequisition.Item> itemList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "pur_purchase_requisition_item", comment = "采购申请行项目")
    public static class Item extends BaseEntity implements MaterialDescriptionHandler, BatchHandler {

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
        @Column(comment = "交货日期")
        LocalDate deliveryDate;

        @Column(comment = "备注")
        String remark;

        @Column(value = "is_purchase_send", comment = "采购直发")
        Boolean purchaseSend;

        @Column(value = "is_complete", comment = "完成")
        Boolean complete;

        Long purchaseRequisitionId;

        @Column(updatable = false)
        String purchaseRequisitionCode;

        Long batchId;

        String batchCode;

        ReferenceTypeEnum referenceType;

        Long referenceDocumentId;

        String referenceDocumentCode;

        Long referenceId;

        Long scheduleId;


        /**
         * 特征值
         */
        @Transient
        @Select(table = "mm_classification", joinValue = "material_id", referencePropertyName = "materialId")
        private List<Classification> classificationList;

        @Transient
        MaterialDescription materialDescription;

    }
}

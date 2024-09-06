package com.rick.admin.module.inventory.entity;

import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchHandler;
import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.admin.module.material.service.MaterialDescriptionHandler;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/6/11 12:20
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "inv_stock", comment = "库存")
public class Stock extends BaseEntity<Long> implements BatchHandler, MaterialDescriptionHandler {

    @NotNull
    private Long plantId;

    @NotNull
    private Long materialId;

    private Long batchId;

    private String batchCode;

    @NotNull
    private BigDecimal quantity;

    @NotBlank
    private String unit;

    @Transient
    private List<Classification> classificationList;

    @Transient
    MaterialDescription materialDescription;

    @Override
    public String getMaterialCode() {
        return materialDescription.getCode();
    }
}
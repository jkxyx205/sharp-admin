package com.rick.admin.module.purchase.entity;

import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.admin.module.material.service.MaterialDescriptionHandler;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * @author Rick.Xu
 * @date 2023/8/1 09:55
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "pur_source_list", comment = "货源清单")
public class MaterialSource extends BaseEntity implements MaterialDescriptionHandler {

    @NotNull
    Long partnerId;

    Long materialId;

    Long materialCategoryId;

    @Transient
    MaterialDescription materialDescription;

}
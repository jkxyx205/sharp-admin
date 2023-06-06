package com.rick.admin.module.inventory.entity;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

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
public class InventoryDocument extends BaseEntity {

    String remark;
}
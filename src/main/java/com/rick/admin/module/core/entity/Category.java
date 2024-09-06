package com.rick.admin.module.core.entity;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

/**
 * @author Rick.Xu
 * @date 2023/6/1 13:37
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "core_material_category", comment = "物料分类")
public class Category extends BaseEntity<Long> {

    private Long parentId;

    @NotEmpty
    private String name;

    private Integer orderIndex;
}
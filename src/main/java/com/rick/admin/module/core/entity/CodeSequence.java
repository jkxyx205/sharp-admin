package com.rick.admin.module.core.entity;

import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick.Xu
 * @date 2023/10/10 17:22
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "core_code_sequence", comment = "序列表")
public class CodeSequence extends SimpleEntity {

    String prefix;

    String name;

    Integer sequence;
}

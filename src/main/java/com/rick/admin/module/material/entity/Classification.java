package com.rick.admin.module.material.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-10-26 10:08:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "mm_classification", comment = "物料分类")
public class Classification extends BaseEntity {

    @NotNull(message = "物料id不能为空")
    private Long materialId;

    @NotNull(message = "classification不能为空")
    @ManyToOne(value = "classification_id", parentTable = "core_classification")
    private com.rick.admin.module.core.entity.Classification classification;

    @NotNull(message = "物料classificationCode不能为空")
    private String classificationCode;

    /**
     * 前端收集数据，赋值给Material实体级联去更新
     */
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<CharacteristicValue> characteristicValueList;

}

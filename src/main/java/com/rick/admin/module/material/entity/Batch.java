package com.rick.admin.module.material.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/7/20 17:39
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "mm_batch", comment = "物料批次")
public class Batch extends BaseEntity<Long> {

    @Length(max = 512, message = "编号不能超过512个字符")
    @NotNull
    @Pattern(regexp = "^[0-9a-zA-Z_/%-]{1,}$", message = "CODE只能包含数字、字母、下划线、中划线")
    @Column(value = "code", updatable = false, comment = "外部可见，唯一code", nullable = false)
    private String code;

    @Column(updatable = false)
    Long materialId;

    @NotBlank
    @Column(updatable = false)
    String materialCode;

    @OneToMany(subTable = "mm_characteristic_value", cascadeInsertOrUpdate = true, joinValue = "reference_id", reversePropertyName="referenceId")
    List<CharacteristicValue> characteristicValueList;

    @ManyToOne(value = "profile_id", parentTable = "mm_profile", comment = "profile信息")
    @JsonIgnore
    private MaterialProfile materialProfile;

    @Select(table = "mm_classification", joinValue = "material_id", referencePropertyName = "materialId")
    private List<Classification> classificationList;

}
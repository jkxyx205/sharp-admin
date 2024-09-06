package com.rick.admin.module.material.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Id;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 非批次物料在单一库房下只有一个库存实例profile
 批次物料在单一批次下只有一个库存实例profile
 * @author Rick
 * @createdAt 2023-02-23 16:58:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "mm_profile", comment = "物料多级profile")
public class MaterialProfile extends BaseEntity<Long> {

    private MaterialProfile.CategoryEnum category;
    
    @Id(strategy = Id.GenerationType.ASSIGN)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long rootId;

    private Long parentId;

    private Integer level;

    @Column(updatable = false)
    private Long materialId;

    @NotBlank
    @Column(updatable = false)
    private String materialCode;

    @Column(updatable = false)
    private Long batchId;

    @Column(updatable = false)
    private String batchCode;

    private Long bomId;

    @OneToMany(subTable = "mm_characteristic_value", cascadeInsertOrUpdate = true, joinValue = "reference_id", reversePropertyName="referenceId")
    @JsonIgnore
    private List<CharacteristicValue> characteristicValueList;

    @AllArgsConstructor
    @Getter
    public enum CategoryEnum {
        MATERIAL("物料"),
        BATCH("批次");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static CategoryEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

}

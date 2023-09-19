package com.rick.admin.module.produce.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.admin.module.material.service.MaterialDescriptionHandler;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/7/25 13:41
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "produce_bom_template", comment = "BOM 模版")
public class BomTemplate extends BaseCodeEntity {

    String description;

    @OneToMany(subTable = "produce_bom_template_component", cascadeInsertOrUpdate = true, joinValue = "bom_template_id", reversePropertyName="bomTemplateId")
    List<Component> componentList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "produce_bom_template_component", comment = "BOM 模版组件")
    public static class Component extends BaseEntity {

        String description;

        BigDecimal quantity;

        String unit;

        @OneToMany(subTable = "produce_bom_template_component_detail", cascadeInsertOrUpdate = true, joinValue = "component_id", reversePropertyName="componentId")
        List<ComponentDetail> componentDetailList;

        Long bomTemplateId;

        @Transient
        String unitText;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @SuperBuilder
    @Table(value = "produce_bom_template_component_detail", comment = "BOM 模版组件详情")
    public static class ComponentDetail extends BaseEntity implements MaterialDescriptionHandler {

        TypeEnum type;

        Long typeInstanceId;

        String placeholder;

        BigDecimal quantity;

        Long componentId;

        @Column(comment = "是否必填", value = "is_required")
        Boolean required;

        @Column(comment = "是否允许修改数量", value = "is_allow_modify_quantity")
        Boolean modifyQuantity;

        @Transient
        ProduceOrder.Item.Detail value;

        @Transient
        BomTemplate bomTemplate;

        @Override
        public Long getMaterialId() {
            return type == TypeEnum.MATERIAL ? typeInstanceId : null;
        }

        @Transient
        MaterialDescription materialDescription;

        /**
         * 物料特征值
         */
        @Select(table = "mm_classification", joinValue = "material_id", referencePropertyName = "typeInstanceId")
        private List<Classification> classificationList;

        public boolean hasTemplate() {
            return Objects.nonNull(bomTemplate);
        }

    }

    @AllArgsConstructor
    @Getter
    public enum TypeEnum {
        CATEGORY("分类"),
        MATERIAL("物料");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static BomTemplate.TypeEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

}
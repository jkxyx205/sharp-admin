package com.rick.admin.module.produce.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

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

    @Transient
    Bom bom;

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
    public static class ComponentDetail extends BaseEntity {

        TypeEnum type;

        Long typeInstanceId;

        String placeholder;

        BigDecimal quantity;

        Long componentId;

        @Transient
        Bom.Item bomItem;

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
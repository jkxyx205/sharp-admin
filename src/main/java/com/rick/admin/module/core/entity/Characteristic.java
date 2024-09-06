package com.rick.admin.module.core.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.common.util.JsonUtils;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Rick.Xu
 * @date 2023/7/20 16:06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "core_characteristic", comment = "特征")
public class Characteristic extends BaseCodeEntity<Long> {

    @Column(comment = "描述")
    private String description;

    @Column(comment = "特征值类型")
    private CharacteristicTypeEnum type;

    @ManyToOne(value = "config_id", parentTable = "sys_form_configurer", comment = "特征值配置", cascadeInsertOrUpdate = true)
    private CpnConfigurer cpnConfigurer;

    @AllArgsConstructor
    @Getter
    public enum CharacteristicTypeEnum {
        TEXT("文本"),
        NUMBER("数字"),
        CURRENCY("金额"),
        DATE("日期"),
        TIME("时间");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static CharacteristicTypeEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }

    /**
     * 获取特征值
     * @return
     */
    public Object getValue() {
        if (StringUtils.isEmpty(cpnConfigurer.getDefaultValue())) {
            return null;
        }

        if (cpnConfigurer.getCpnType() == CpnTypeEnum.CHECKBOX) {
            return JsonUtils.toList(cpnConfigurer.getDefaultValue(), String.class);
        }

        return cpnConfigurer.getDefaultValue();
    }
}
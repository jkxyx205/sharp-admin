package com.rick.admin.module.material.entity;

import com.rick.common.util.JsonUtils;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Rick
 * @createdAt 2022-06-25 13:23:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "mm_characteristic_value", comment = "特征值的value")
public class CharacteristicValue extends BaseEntity<Long> {

    @Column(comment = "PROFILE：库存实例PROFILE表id")
    private Long referenceId;

    private Long classificationId;

    private String classificationCode;

    private Long characteristicId;

    private String characteristicCode;

    private String value;

    @Transient
    private Object val;


    public String getValue() {
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        if (val == null) {
            return null;
        }
        if (val.getClass() == String.class) {
            return (String) val;
        }

        return JsonUtils.toJson(val);
    }
}
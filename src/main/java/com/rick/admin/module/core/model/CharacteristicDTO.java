package com.rick.admin.module.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rick.admin.module.core.entity.Characteristic;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2022-06-25 13:36:00
 */
@Data
@Builder
public class CharacteristicDTO {

    private Long id;

    @NotBlank
    private String code;

    @NotBlank
    private String description;

    @NotNull
    private Characteristic.CharacteristicTypeEnum type;

    private List<CpnConfigurer.CpnOption> options;

    @NotNull
    private CpnTypeEnum cpnType;

    @NotNull
    private Boolean required;

    private String placeholder;

    private String pattern;

    /**
     * 控件值
     */
    private Object value;

    @JsonIgnore
    public Map<String, Object> getAdditionalInfo() {
        Map<String, Object> additionalInfo = null;

        if (Characteristic.CharacteristicTypeEnum.NUMBER == type) {

        } else if (Characteristic.CharacteristicTypeEnum.CURRENCY == type) {

        }
        return null;
    }

}

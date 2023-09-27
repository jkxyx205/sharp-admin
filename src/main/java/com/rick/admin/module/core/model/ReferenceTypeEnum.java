package com.rick.admin.module.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Rick.Xu
 * @date 2023/9/25 16:44
 */
@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReferenceTypeEnum {
    SO("销售订单"),
    PR("采购申请");

    public String getCode() {
        return this.name();
    }

    private final String label;

    public static ReferenceTypeEnum valueOfCode(String code) {
        return valueOf(code);
    }
}
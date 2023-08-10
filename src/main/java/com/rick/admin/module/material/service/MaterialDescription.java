package com.rick.admin.module.material.service;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Rick.Xu
 * @date 2023/8/2 00:08
 */
@Data
public class MaterialDescription {

    Long id;

    String code;

    String name;

    String specification;

    String unit;

    String unitText;

    Long categoryId;

    BigDecimal unitPrice;

    public MaterialDescription(Long id) {
        this.id = id;
    }
}


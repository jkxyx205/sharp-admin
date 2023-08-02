package com.rick.admin.module.material.service;

/**
 * @author Rick.Xu
 * @date 2023/8/2 00:08
 */
public interface MaterialDescription {

    Long getMaterialId();

    void setMaterialCode(String materialCode);

    void setMaterialText(String materialText);

    default void setUnitText(String unitText) {}

    default void setUnit(String unit) {}

    default void setMaterialCategoryId(Long categoryId) {};
}

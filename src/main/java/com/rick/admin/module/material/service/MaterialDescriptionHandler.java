package com.rick.admin.module.material.service;

/**
 * @author Rick.Xu
 * @date 2023/8/2 00:08
 */
public interface MaterialDescriptionHandler {

    Long getMaterialId();

    void setMaterialDescription(MaterialDescription materialDescription);

    MaterialDescription getMaterialDescription();

    default String getMaterialCode() {
        return getMaterialDescription() == null ? "" : getMaterialDescription().getCode();
    }

    default String getMaterialText() {
        return getMaterialName() + " " + getMaterialSpecification();
    }

    default String getMaterialName() {
        return getMaterialDescription() == null ? "" : getMaterialDescription().getName();
    }

    default String getMaterialSpecification() {
        return getMaterialDescription() == null ? "" : getMaterialDescription().getSpecification();
    }

    default String getUnitText() {
        return getMaterialDescription() == null ? "" : getMaterialDescription().getUnitText();
    }

    default String getCharacteristic() {
        return getMaterialDescription() == null ? "" : getMaterialDescription().getCharacteristic();
    }

    default Long getBatchId() {
        return getMaterialDescription() == null ? null : getMaterialDescription().getBatchId();
    }

}

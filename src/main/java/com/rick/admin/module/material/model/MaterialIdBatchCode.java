package com.rick.admin.module.material.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/8/4 22:35
 */
@AllArgsConstructor
@Getter
public class MaterialIdBatchCode {

    private Long materialId;

    private String batchCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialIdBatchCode that = (MaterialIdBatchCode) o;

        if (!materialId.equals(that.materialId)) return false;
        return Objects.equals(batchCode, that.batchCode);
    }

    @Override
    public int hashCode() {
        int result = materialId.hashCode();
        result = 31 * result + (batchCode != null ? batchCode.hashCode() : 0);
        return result;
    }
}

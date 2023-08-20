package com.rick.admin.module.material.service;

import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/8/21 00:13
 */
@UtilityClass
public class MaterialProfileSupport {

    public String materialIdBatchIdString(long materialId, Long batchId) {
        return materialId + Objects.toString(batchId, "");
    }
}

package com.rick.admin.module.material.service;

import com.rick.admin.module.material.entity.Classification;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/8/19 15:07
 */
public interface BatchHandler {

    List<Classification>  getClassificationList();

    void setBatchId(Long batchId);

    void setBatchCode(String batchCode);

    Long getBatchId();

    Long getMaterialId();

    String getMaterialCode();

}

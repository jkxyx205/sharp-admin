package com.rick.admin.module.report.model;

import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchHandler;
import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.admin.module.material.service.MaterialDescriptionHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/9/4 10:10
 */

@Data
public class StockItem implements MaterialDescriptionHandler, BatchHandler {

    private Long categoryId;

    private String categoryName;

    private Long materialId;

    private Long batchId;

    private String batchCode;

    private MaterialDescription materialDescription;

    private List<Classification> classificationList;

    private BigDecimal quantity;


    @Override
    public String getMaterialCode() {
        return MaterialDescriptionHandler.super.getMaterialCode();
    }

    @Override
    public String getMaterialText() {
        return MaterialDescriptionHandler.super.getMaterialText();
    }

    @Override
    public String getMaterialName() {
        return MaterialDescriptionHandler.super.getMaterialName();
    }

    @Override
    public String getMaterialSpecification() {
        return MaterialDescriptionHandler.super.getMaterialSpecification();
    }

    @Override
    public String getUnitText() {
        return MaterialDescriptionHandler.super.getUnitText();
    }

    @Override
    public String getCharacteristic() {
        return MaterialDescriptionHandler.super.getCharacteristic();
    }

}

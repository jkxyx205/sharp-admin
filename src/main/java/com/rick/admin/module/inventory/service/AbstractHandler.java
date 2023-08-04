package com.rick.admin.module.inventory.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.dao.InventoryDocumentDAO;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.entity.Stock;
import com.rick.admin.module.material.entity.Batch;
import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/11 11:42
 */
@RequiredArgsConstructor
public abstract class AbstractHandler implements MovementHandler {

    @Resource
    protected InventoryDocumentDAO inventoryDocumentDAO;

    @Resource
    protected StockService stockService;

    @Resource
    private BatchService batchService;

    @Override
    public void handle(InventoryDocument inventoryDocument) {
        inventoryDocument.setCode(CodeHelper.generateCode("MD"));
        inventoryDocument.setCanceled(false);

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            item.setId(null);
            item.setType(inventoryDocument.getType());
            item.setReferenceType(inventoryDocument.getReferenceType());
            item.setReferenceCode(inventoryDocument.getReferenceCode());
            item.setInventoryDocumentCode(inventoryDocument.getCode());
            item.setPlantId(inventoryDocument.getPlantId());
        }

        handle0(inventoryDocument);
        inventoryDocument.setItemList(inventoryDocument.getItemList().stream().filter(item -> BigDecimalUtils.gt(item.getQuantity(), BigDecimal.ZERO)).collect(Collectors.toList()));

        if (CollectionUtils.isEmpty(inventoryDocument.getItemList())) {
            inventoryDocument.setCode(null);
            return;
        }

        inventoryDocumentDAO.insert(inventoryDocument);

        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            // 如果是批次物料，创建批次主数据
            Long batchId = null;
            if (StringUtils.isNotBlank(item.getBatchCode())) {
                Batch batch = Batch.builder()
                        .code(item.getBatchCode())
                        .classificationList(Arrays.asList(
                                Classification.builder().classificationCode("COLOR")
                                        .characteristicValueList(Arrays.asList(
                                                CharacteristicValue.builder()
                                                        .classificationCode("COLOR")
                                                        .characteristicCode("COLOR")
                                                        .val(item.getColor()).build()
                                        ))
                                        .build()
                        ))
                        .materialCode(item.getMaterialCode())
                        .build();
                batchService.saveOrUpdate(batch);
                batchId = batch.getId();
                item.setBatchId(batchId);
            }

            stockService.changeStockQuantity(Stock.builder()
                    .plantId(item.getPlantId())
                    .materialId(item.getMaterialId())
                    .batchId(batchId)
                    .batchCode(item.getBatchCode())
                    .quantity(item.getMovementType() == InventoryDocument.MovementTypeEnum.OUTBOUND ? BigDecimal.ZERO.subtract(item.getQuantity()) : item.getQuantity())
                    .unit(item.getUnit())
                    .build());
        }
    }

    public abstract void handle0(InventoryDocument inventoryDocument);

}

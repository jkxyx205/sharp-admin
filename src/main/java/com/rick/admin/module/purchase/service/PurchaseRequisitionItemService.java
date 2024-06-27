package com.rick.admin.module.purchase.service;

import com.rick.admin.module.purchase.entity.PurchaseRequisition;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/9/25 17:00
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class PurchaseRequisitionItemService {

    EntityDAO<PurchaseRequisition.Item, Long> purchaseRequisitionItemDAO;

    public Map<String, Integer> requisitionQuantityMapping() {
        List<PurchaseRequisition.Item> items = purchaseRequisitionItemDAO.selectByParamsWithoutCascade(Collections.emptyMap(),
                "material_id, batch_id, quantity"
                ,"is_complete = 0 and is_deleted = 0");

        Map<String, Integer> requisitionQuantityMapping = items.stream().collect(Collectors.groupingBy(item -> item.getMaterialId() + (Objects.isNull(item.getBatchId()) ? "" : "" + item.getBatchId()),
                Collectors.summingInt(item -> item.getQuantity().intValue()))
        );

        return requisitionQuantityMapping;
    }

    public int deleteByIds(String ids) {
//        return purchaseRequisitionItemDAO.deleteByIds(ids);
        return purchaseRequisitionItemDAO.deleteLogicallyByIds(ids);
    }

    public void insertOrUpdateByReferenceIds(List<PurchaseRequisition.Item> itemList) {
//        Map<Long, PurchaseRequisition.Item> referenceIdEntityMap = getReferenceIdEntityMap(itemList.stream().map(PurchaseRequisition.Item::getReferenceId).collect(Collectors.toSet()));
//        itemList.forEach(item -> {
//            PurchaseRequisition.Item prItem = referenceIdEntityMap.get(item.getReferenceId());
//            if (Objects.nonNull(prItem)) {
//                Boolean complete = prItem.getComplete();
//                Long id = prItem.getId();
//                item.setComplete(complete);
//                item.setId(id);
//            }
//        });

//
//        Map<Long, Long> referenceIdEItemIdMap = itemList.stream()
//                .filter(item -> Objects.nonNull(item.getId())).collect(Collectors.toMap(PurchaseRequisition.Item::getReferenceId, PurchaseRequisition.Item::getId));
//
//        for (Map.Entry<Long, Long> referenceIdEItemIdMapEntry : referenceIdEItemIdMap.entrySet()) {
//            purchaseRequisitionItemDAO.delete(Params.builder(2)
//                            .pv("referenceId", referenceIdEItemIdMapEntry.getKey())
//                            .pv("ids", referenceIdEItemIdMapEntry.getValue())
//                            .build()
//                    , "reference_id = :referenceId AND id not IN (:ids)");
//        }

        purchaseRequisitionItemDAO.insertOrUpdate(itemList);
    }


//    public Map<Long, PurchaseRequisition.Item> getReferenceIdEntityMap(Collection<Long> referenceIds) {
//        return purchaseRequisitionItemDAO.selectByParams(Params.builder(1).pv("referenceIds",
//                        referenceIds).build(), "reference_id IN (:referenceIds)")
//                .stream().collect(Collectors.toMap(PurchaseRequisition.Item::getReferenceId, Function.identity()));
//    }


    public void markCompleted(Collection<Long> ids) {
        Assert.notEmpty(ids, "ids cannot be empty");

        purchaseRequisitionItemDAO.update("is_complete",
                Params.builder(2).pv("ids", ids).pv("is_complete", true).build(),
                "id IN (:ids)");
    }

    public void deleteUnCompletePurchaseRequisitionByReferenceDocumentCode(String referenceDocumentCode) {
        purchaseRequisitionItemDAO.delete(new Object[]{referenceDocumentCode}, "reference_document_code = ? AND is_complete = 0");
    }
}

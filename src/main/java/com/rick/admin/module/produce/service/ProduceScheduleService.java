package com.rick.admin.module.produce.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.handler.ProduceOrderConsumeHandler;
import com.rick.admin.module.inventory.service.handler.ProduceOrderScheduleInboundHandler;
import com.rick.admin.module.material.entity.CharacteristicValue;
import com.rick.admin.module.material.entity.Classification;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.produce.dao.ProduceOrderItemDAO;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.db.util.OptionalUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/9/21 01:53
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class ProduceScheduleService {

    EntityCodeDAO<ProduceOrder.Item.Schedule, Long> produceOrderItemScheduleDAO;

    SharpService sharpService;

    ProduceOrderScheduleInboundHandler produceOrderScheduleInboundHandler;

    ProduceOrderConsumeHandler produceOrderConsumeHandler;

    ProduceOrderItemDAO produceOrderItemDAO;

    BomService bomService;

    BatchService batchService;

    ProduceOrderService produceOrderService;

    @Transactional(rollbackFor = Exception.class)
    public void markStatus(Long scheduleId) {
//        String status = produceOrderItemScheduleDAO.selectSingleValueById(scheduleId, "status", String.class).get();
        ProduceOrder.Item.Schedule simpleSchedule = OptionalUtils.expectedAsOptional(produceOrderItemScheduleDAO.selectByParamsWithoutCascade(Params.builder(1).pv("id", scheduleId).build(), "id, code, status, produce_order_id, produce_order_code, quantity, unit, produce_order_item_id", "id = :id")).get();
        produceOrderItemScheduleDAO.update("status", new Object[]{simpleSchedule.getStatus() == ProduceOrder.StatusEnum.PRODUCING ? ProduceOrder.StatusEnum.PRODUCED.name() : ProduceOrder.StatusEnum.PRODUCING.name(), scheduleId}, "id = ?");


        if (simpleSchedule.getStatus() == ProduceOrder.StatusEnum.PRODUCING) {
            // 生产完成自动入成品库
            produceOrderScheduleInboundHandler.handle(inbound(simpleSchedule));
            // 扣减用料
            produceOrderConsumeHandler.handle(outbound(simpleSchedule));
        }

//        Long orderId = sharpService.queryForObject("select produce_order.id from produce_order_item, produce_order_item_schedule, produce_order \n" +
//                "where produce_order_item.id = produce_order_item_schedule.`produce_order_item_id` \n" +
//                "AND produce_order.id = produce_order_item.`produce_order_id`\n" +
//                "AND produce_order_item_schedule.id = :scheduleId ", Params.builder(1).pv("scheduleId", scheduleId).build(), Long.class).get();

        // 如果所有的都生产完成标记订单生产完成
//        List<String> statusList = sharpService.query("select produce_order_item_schedule.status from produce_order_item, produce_order_item_schedule, produce_order po\n" +
//                "where produce_order_item.id = produce_order_item_schedule.`produce_order_item_id` \n" +
//                "AND po.id = produce_order_item.`produce_order_id`\n" +
//                "AND exists (select 1 from produce_order_item, produce_order_item_schedule, produce_order \n" +
//                "where produce_order_item.id = produce_order_item_schedule.`produce_order_item_id` \n" +
//                "AND produce_order.id = produce_order_item.`produce_order_id`\n" +
//                "AND produce_order.id = po.id\n" +
//                "AND produce_order_item_schedule.id = :scheduleId \n" +
//                ")", Params.builder(1).pv("scheduleId", scheduleId).build(), String.class);

        produceOrderService.setStatusIfAllProduced(simpleSchedule.getProduceOrderId(), simpleSchedule.getProduceOrderCode());
    }

    public String findActiveProduceScheduleByKeyCode(String keyCode) {
        List<String> codeList = produceOrderItemScheduleDAO.selectByParams(Params.builder(1).pv("code", keyCode).build(),
                "code",
                "status <> 'PLANNING' AND code LIKE CONCAT('%',:code) AND TIMESTAMPDIFF(DAY, update_time, now()) < 360", String.class);

        if (codeList.size() == 0) {
            throw new BizException("没有找到订单号，请输入完整订单号！");
        } else if (codeList.size() > 1) {
            throw new BizException("系统找到了多个订单号，请输入更多的编号来确定唯一订单号");
        }

        return codeList.get(0);
    }

    public Map<Long, BigDecimal> openQuantity(InventoryDocument.MovementTypeEnum movementType, String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode AND plant_id = 719893335619162112 group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        // language=SQL
//        String sql2 = "select produce_order_item_detail.`id`, produce_order_item_detail.quantity * produce_order_item_schedule.quantity from produce_order_item_schedule\n" +
//                "                inner join produce_order_item on produce_order_item.id = produce_order_item_schedule.produce_order_item_id\n" +
//                "                inner join produce_order_item_detail on produce_order_item.id = produce_order_item_detail.`produce_order_item_id`\n" +
//                "                where produce_order_item_schedule.code = :rootReferenceCode";
//
//        Map<Long, BigDecimal> requestQuantityMap = sharpService.queryForKeyValue(sql2, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());
        List<ProduceOrder.Item.Detail> detailList = flatDetailList(rootReferenceCode);
        Map<Long, BigDecimal> requestQuantityMap = detailList.stream().collect(Collectors.toMap(ProduceOrder.Item.Detail::getId, ProduceOrder.Item.Detail::getQuantity));

        if (movementType == InventoryDocument.MovementTypeEnum.OUTBOUND) {
            // 同向
            for (Map.Entry<Long, BigDecimal> entry : requestQuantityMap.entrySet()) {
                BigDecimal value = entry.getValue().subtract(ObjectUtils.defaultIfNull(histroyGoodsReceiptQuantityMap.get(entry.getKey()), BigDecimal.ZERO));
                histroyGoodsReceiptQuantityMap.put(entry.getKey(), BigDecimalUtils.lt(value, BigDecimal.ZERO) ? BigDecimal.ZERO : value);
            }
        } else {
            for (Long itemId : requestQuantityMap.keySet()) {
                if (!histroyGoodsReceiptQuantityMap.containsKey(itemId)) {
                    histroyGoodsReceiptQuantityMap.put(itemId, BigDecimal.ZERO);
                }
            }
        }

        return histroyGoodsReceiptQuantityMap;
    }

    public List<ProduceOrder.Item.Detail> flatDetailList(String scheduleCode) {
        ProduceOrder.Item.Schedule schedule = produceOrderItemScheduleDAO.selectByCode(scheduleCode).get();
        ProduceOrder.Item item = produceOrderItemDAO.selectById(schedule.getProduceOrderItemId()).get();
        BomTemplate bomTemplate = resolveItemAndReturnBomTemplate(item);

        List<ProduceOrder.Item.Detail> detailList = Lists.newArrayList();
        fetchBomList(schedule.getQuantity(), bomTemplate, detailList);
        return detailList;
    }

    private InventoryDocument inbound(ProduceOrder.Item.Schedule simpleSchedule) {
        Long produceOrderItemId = simpleSchedule.getProduceOrderItemId();
        Map<String, Object> values = produceOrderItemDAO.selectSingleValueById(produceOrderItemId, "material_id, material_code, batch_id, batch_code", Map.class).get();

        InventoryDocument inventoryDocument = InventoryDocument.builder()
                .type(InventoryDocument.TypeEnum.INBOUND)
                .referenceType(InventoryDocument.ReferenceTypeEnum.PP)
                .referenceCode(simpleSchedule.getCode())
                .rootReferenceCode(simpleSchedule.getProduceOrderCode())
                .remark("生产入库")
                .plantId(726159086739001344L)
                .documentDate(LocalDate.now())
                .itemList(Arrays.asList(
                        InventoryDocument.Item.builder()
                                .materialId((Long) values.get("material_id"))
                                .materialCode((String) values.get("material_code"))
                                .batchId((Long) values.get("batch_id"))
                                .batchCode((String) values.get("batch_code"))
                                .referenceType(InventoryDocument.ReferenceTypeEnum.PP)
                                .referenceCode(simpleSchedule.getCode())
                                .referenceItemId(produceOrderItemId)
                                .rootReferenceItemId(produceOrderItemId)
                                .rootReferenceCode(simpleSchedule.getCode())
                                .type(InventoryDocument.TypeEnum.INBOUND)
                                .movementType(InventoryDocument.MovementTypeEnum.INBOUND)
                                .quantity(simpleSchedule.getQuantity())
                                .unit(simpleSchedule.getUnit())
                                .build()
                ))
                .build();

        return inventoryDocument;
    }

    private InventoryDocument outbound(ProduceOrder.Item.Schedule simpleSchedule) {
        String referenceCode= simpleSchedule.getCode();
        String sql = "select produce_order_item_detail.`material_id`,\n" +
                "       true allowNegativeStock," +
                "       mm_material.code material_code,       produce_order_item_detail.batch_id,       produce_order_item_detail.batch_code,       produce_order_item_detail.`id`                               referenceItemId,\n" +
                "       produce_order_item_detail.`id`                               rootReferenceItemId,\n" +
                "       ((CASE\n" +
                "WHEN produce_order_item_detail.component_detail_id = 725451860537794560 THEN 3 * produce_order_item_detail.quantity\n" +
                "WHEN produce_order_item_detail.component_detail_id = 725451860537794561 THEN 3 * produce_order_item_detail.quantity\n" +
                "ELSE produce_order_item_detail.quantity\n" +
                "END) * produce_order_item_schedule.quantity)  quantity,\n" +
                "       mm_material.base_unit                                          unit\n" +
                "from produce_order_item_schedule\n" +
                "         inner join produce_order_item on produce_order_item.id = produce_order_item_schedule.produce_order_item_id\n" +
                "         inner join produce_order_item_detail on produce_order_item.id = produce_order_item_detail.`produce_order_item_id`\n" +
                "         inner join mm_material on mm_material.id = produce_order_item_detail.`material_id`\n" +
                "where produce_order_item_schedule.code = :referenceCode";

        List<InventoryDocument.Item> itemList = sharpService.query(sql, Params.builder(1).pv("referenceCode", referenceCode).build(), InventoryDocument.Item.class);
        if (CollectionUtils.isEmpty(itemList)) {
            throw new BizException(ExceptionCodeEnum.PP_DOCUMENT_NOT_FOUND_ERROR, new Object[]{referenceCode});
        }

        batchService.handleClassificationAndFillCharacteristicValue(itemList);

        itemList.stream().filter(item -> CollectionUtils.isNotEmpty(item.getClassificationList())).forEach(item -> {
            for (Classification classification : item.getClassificationList()) {
                classification.setCharacteristicValueList(classification.getClassification().getCharacteristicList().stream()
                        .map(characteristic -> CharacteristicValue.builder().characteristicCode(characteristic.getCode())
                                .characteristicId(characteristic.getId())
                                .val(characteristic.getCpnConfigurer().getDefaultValue())
                                .build())
                        .collect(Collectors.toList()));
            }
        });

        InventoryDocument inventoryDocument = InventoryDocument.builder()
                .type(InventoryDocument.TypeEnum.CONSUME)
                .referenceType(InventoryDocument.ReferenceTypeEnum.PP)
                .referenceCode(referenceCode)
                .rootReferenceCode(simpleSchedule.getProduceOrderCode())
                .remark("用料出库")
                .plantId(726158903766683648L)
                .documentDate(LocalDate.now())
                .itemList(itemList)
                .build();

        // 处理线的供应商
        for (InventoryDocument.Item item : inventoryDocument.getItemList()) {
            if (item.getMaterialId() == 729584784212238336L
                    || item.getMaterialId() == 741996205273632769L
                    || item.getMaterialId() == 731499486144483329L) {
                String scheduleCode = inventoryDocument.getReferenceCode();

                String batchIdSql = "select batch_id from inv_document_item where type = 'MOVING_TO_PRODUCE'\n" +
                        "and reference_type = 'PP' and movement_type = 'OUTBOUND' and reference_code = :reference_code and material_code = :material_code and reference_item_id = :reference_item_id LIMIT 1";
                Optional<Map<String, Object>> optional = sharpService.queryForObject(batchIdSql, Params.builder(3)
                        .pv("reference_code", scheduleCode)
                        .pv("material_code", item.getMaterialCode())
                        .pv("reference_item_id", item.getReferenceItemId())
                        .build());
                if (optional.isPresent()) {
                    String lineBrandSql = "select mm_characteristic_value.value from inv_document_item, mm_profile, mm_characteristic_value where mm_profile.id = mm_characteristic_value.reference_id and mm_profile.batch_id = :batch_id and characteristic_code = 'LINE_BRAND' LIMIT 1";

                    List<Map<String, Object>> list = sharpService.query(lineBrandSql, Params.builder(1)
                        .pv("batch_id", optional.get().get("batch_id")).build());

                    if (CollectionUtils.isNotEmpty(list)) {
//                    item.setBatchId((Long) list.get(0).get("batch_id"));
//                    item.setBatchCode((String) list.get(0).get("batch_code"));
                        for (Classification classification : item.getClassificationList()) {
                            for (CharacteristicValue characteristicValue : classification.getCharacteristicValueList()) {
                                if (characteristicValue.getCharacteristicCode().equals("LINE_BRAND")) {
                                    characteristicValue.setVal(list.get(0).get("value"));
                                }
                            }
                        }
                    }
                }
// 一条SQL语句获取供应商信息（性能有问题）
//                String lineBrandSql = "select inv_document_item.batch_id batchId, inv_document_item.batch_code batchCode, mm_characteristic_value.value from inv_document_item, mm_profile, mm_characteristic_value where inv_document_item.batch_id = mm_profile.batch_id and mm_profile.id = mm_characteristic_value.reference_id and type = 'MOVING_TO_PRODUCE'\n" +
//                        "and reference_type = 'PP' and movement_type = 'OUTBOUND' and reference_code = :reference_code and inv_document_item.material_code = :material_code and reference_item_id = :reference_item_id and characteristic_code = 'LINE_BRAND' LIMIT 1";

//                List<Map<String, Object>> list = sharpService.query(lineBrandSql, Params.builder(3)
//                        .pv("reference_code", scheduleCode)
//                        .pv("material_code", item.getMaterialCode())
//                        .pv("reference_item_id", item.getReferenceItemId()).build());
            }
        }

        return inventoryDocument;
    }

    private BomTemplate resolveItemAndReturnBomTemplate(ProduceOrder.Item item) {
        Map<Long, ProduceOrder.Item.Detail> valueMapping = item.getItemList().stream().collect(Collectors.toMap(ProduceOrder.Item.Detail::getComponentDetailId, v -> v));

        BomTemplate bomTemplate = bomService.getBomTemplateMaterialId(item.getMaterialId(), valueMapping, false);
        return bomTemplate;
    }

    private void fetchBomList(BigDecimal quantity, BomTemplate bomTemplate, List<ProduceOrder.Item.Detail> detailList) {
        for (BomTemplate.Component component : bomTemplate.getComponentList()) {
            for (BomTemplate.ComponentDetail componentDetail : component.getComponentDetailList()) {
                BomTemplate subBomTemplate = componentDetail.getBomTemplate();
                if (Objects.nonNull(subBomTemplate)) {
                    fetchBomList(quantity.multiply(componentDetail.getQuantity()), subBomTemplate, detailList);
                } else {
                    ProduceOrder.Item.Detail value = componentDetail.getValue();
                    if (Objects.nonNull(value.getMaterialId())) {
                       value.setQuantity(value.getQuantity().multiply(quantity));
                       detailList.add(value);
                    }
                }
            }
        }
    }
}
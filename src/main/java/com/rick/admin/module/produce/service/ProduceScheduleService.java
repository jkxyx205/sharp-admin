package com.rick.admin.module.produce.service;

import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.inventory.service.handler.ProduceOrderScheduleInboundHandler;
import com.rick.admin.module.produce.dao.ProduceOrderDAO;
import com.rick.admin.module.produce.dao.ProduceOrderItemDAO;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.db.util.OptionalUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/9/21 01:53
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class ProduceScheduleService {

    EntityDAO<ProduceOrder.Item.Schedule, Long> produceOrderItemScheduleDAO;

    ProduceOrderDAO produceOrderDAO;

    SharpService sharpService;

    ProduceOrderScheduleInboundHandler produceOrderScheduleInboundHandler;

    ProduceOrderItemDAO produceOrderItemDAO;

    @Transactional(rollbackFor = Exception.class)
    public void markStatus(Long scheduleId) {
//        String status = produceOrderItemScheduleDAO.selectSingleValueById(scheduleId, "status", String.class).get();
        ProduceOrder.Item.Schedule simpleSchedule = OptionalUtils.expectedAsOptional(produceOrderItemScheduleDAO.selectByParamsWithoutCascade(Params.builder(1).pv("id", scheduleId).build(), "id, code, status, produce_order_id, quantity, unit, produce_order_item_id", "id = :id")).get();
        produceOrderItemScheduleDAO.update("status", new Object[]{simpleSchedule.getStatus() == ProduceOrder.StatusEnum.PRODUCING ? ProduceOrder.StatusEnum.PRODUCED.name() : ProduceOrder.StatusEnum.PRODUCING.name(), scheduleId}, "id = ?");

        // 生产完成自动入成品库
        if (simpleSchedule.getStatus() == ProduceOrder.StatusEnum.PRODUCING) {
            produceOrderScheduleInboundHandler.handle(inbound(simpleSchedule));
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

        // 如果所有的都生产完成标记订单生产完成
        List<String> statusList = sharpService.query("select produce_order_item_schedule.status from produce_order_item, produce_order_item_schedule, produce_order \n" +
                "where produce_order_item.id = produce_order_item_schedule.`produce_order_item_id` \n" +
                "AND produce_order.id = produce_order_item.`produce_order_id`\n" +
                "AND produce_order.id = :orderId", Params.builder(1).pv("orderId", simpleSchedule.getProduceOrderId()).build(), String.class);

        if (statusList.stream().allMatch(s -> s.equals(ProduceOrder.StatusEnum.PRODUCED.name()))) {
            produceOrderDAO.update("status", new Object[]{ProduceOrder.StatusEnum.PRODUCED.name(), simpleSchedule.getProduceOrderId()}, "id = ? and status <> 'DONE'");
        } else {
            produceOrderDAO.update("status", new Object[]{ProduceOrder.StatusEnum.PRODUCING.name(), simpleSchedule.getProduceOrderId()}, "id = ?");
        }

    }

    public String findActiveProduceScheduleByKeyCode(String keyCode) {
        List<String> codeList = produceOrderItemScheduleDAO.selectByParams(Params.builder(1).pv("code", keyCode).build(),
                "code",
                "status <> 'PLANNING' AND code LIKE CONCAT('%',:code) AND TIMESTAMPDIFF(DAY, update_time, now()) < 90", String.class);

        if (codeList.size() == 0) {
            throw new BizException("没有找到订单号，请输入更多的编号或者完整订单号！");
        } else if (codeList.size() > 1) {
            throw new BizException("系统找到了多个订单号，请输入更多的编号来确定唯一订单号");
        }

        return codeList.get(0);
    }

    public Map<Long, BigDecimal> openQuantity(InventoryDocument.MovementTypeEnum movementType, String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode AND plant_id = 719893335619162112 group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        // language=SQL
        String sql2 = "select produce_order_item_detail.`id`, produce_order_item_detail.quantity * produce_order_item_schedule.quantity from produce_order_item_schedule\n" +
                "                inner join produce_order_item on produce_order_item.id = produce_order_item_schedule.produce_order_item_id\n" +
                "                inner join produce_order_item_detail on produce_order_item.id = produce_order_item_detail.`produce_order_item_id`\n" +
                "                where produce_order_item_schedule.code = :rootReferenceCode";

        Map<Long, BigDecimal> requestQuantityMap = sharpService.queryForKeyValue(sql2, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

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
}
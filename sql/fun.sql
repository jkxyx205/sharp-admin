-- 删除物料的库存信息
delete from `inv_document_item` where material_id = :materialId;
delete from inv_stock where material_id = :materialId;
delete from mm_batch where material_id = :materialId;
delete from mm_characteristic_value where reference_id IN (select id from mm_profile where material_id = :materialId);
delete from mm_profile where material_id = :materialId;

-- 清空产前库库存
delete from inv_stock where plant_id = 726158903766683648

-- 删除已完成的申请
delete
from pur_purchase_requisition_item
where is_complete = 0
  and reference_document_code in (select *
                                  from (select reference_document_code
                                        from pur_purchase_requisition_item
                                        where exists(select 1
                                                     from produce_order
                                                     where produce_order.id = pur_purchase_requisition_item.reference_document_id
                                                       AND status <> 'PRODUCING')) as ee);

-- 查看BOM的物料的需求情况
select produce_order.code,             floor(sum((-1 * schedule.quantity * (CASE
                                                                                WHEN produce_order_item_detail.component_detail_id = 725451860537794560
                                                                                    THEN 3 * produce_order_item_detail.quantity
                                                                                WHEN produce_order_item_detail.component_detail_id = 725451860537794561
                                                                                    THEN 3 * produce_order_item_detail.quantity
                                                                                ELSE produce_order_item_detail.quantity
    END)))) quantity
from produce_order_item,
     produce_order,
     produce_order_item_detail,
     produce_order_item_schedule schedule
where produce_order.id = produce_order_item.produce_order_id
  AND produce_order_item_detail.produce_order_item_id = produce_order_item.id
  AND schedule.produce_order_item_id = produce_order_item.id
  AND schedule.status = 'PRODUCING'
  AND produce_order.`status` = 'PRODUCING'
  AND produce_order_item.item_category = 'PRODUCT' AND produce_order_item_detail.material_id = :materialCode
group by code
order by quantity asc;
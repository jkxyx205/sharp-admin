-- 删除物料的库存信息
delete from `inv_document_item` where material_id = :materialId;
delete from inv_stock where material_id = :materialId;
delete from mm_batch where material_id = :materialId;
delete from mm_characteristic_value where reference_id IN (select id from mm_profile where material_id = :materialId);
delete from mm_profile where material_id = :materialId;

-- 删除已完成销售订单的采购申请
delete
from pur_purchase_requisition_item
where is_complete = 0
  and reference_document_code in (select *
                                  from (select reference_document_code
                                        from pur_purchase_requisition_item
                                        where exists(select 1
                                                     where produce_order.id = pur_purchase_requisition_item.reference_document_id
                                                       AND status <> 'PRODUCING')) as ee);
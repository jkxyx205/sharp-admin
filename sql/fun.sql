-- 删除物料的库存信息
delete from `inv_document_item` where material_id = :materialId;
delete from inv_stock where material_id = :materialId;
delete from mm_batch where material_id = :materialId;
delete from mm_profile where material_id = :materialId;
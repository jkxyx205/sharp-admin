-- dirty data
delete from pur_source_list where not exists(select 1 from core_partner where `partner_type` = 'VENDOR' AND core_partner.id = pur_source_list.partner_id AND core_partner.is_deleted = 0) or (not exists(select 1 from mm_material where mm_material.id = pur_source_list.material_id AND is_deleted = 0) AND pur_source_list.material_id is not null) or not exists (select 1 from `core_material_category` where core_material_category.id = pur_source_list.material_category_id AND core_material_category.is_deleted = 0);

delete from `sys_form_configurer` where id NOT IN (select config_id from sys_form_cpn_configurer) AND name NOT IN (select CODE from `core_characteristic`);

delete from sys_role_permission where not exists(select 1 from sys_role where sys_role.id = sys_role_permission.role_id) or not exists(select 1 from sys_permission where sys_permission.id = sys_role_permission.permission_id);

delete from sys_user_role where not exists(select 1 from sys_role where sys_role.id = sys_user_role.role_id) or not exists(select 1 from sys_user where sys_user.id = sys_user_role.user_id);

delete from produce_bom_template_component where not exists (select 1 from produce_bom_template where id = produce_bom_template_component.`bom_template_id`);

delete from produce_bom_template_component_detail where type = 'MATERIAL' AND not exists(select 1 from mm_material where id = produce_bom_template_component_detail.type_instance_id);

delete from produce_bom_template_component_detail where type = 'CATEGORY' AND not exists(select 1 from core_material_category where id = produce_bom_template_component_detail.type_instance_id);


-- 删除物料， 级联删除
delete from mm_material where is_deleted = 1;
delete from `mm_classification` where not exists(select 1 from mm_material where id = mm_classification.material_id);
delete from `mm_profile` where not exists(select 1 from mm_material where id = mm_profile.material_id);
delete from `mm_batch` where not exists(select 1 from mm_material where id = mm_batch.material_id);
delete from `inv_document_item` where not exists(select 1 from mm_material where id = inv_document_item.material_id);
delete from `inv_stock` where not exists(select 1 from mm_material where id = inv_stock.material_id);

-- 删除物料的关联数据
delete from `mm_characteristic_value` where not exists(select 1 from mm_profile where id = mm_characteristic_value.`reference_id`);
-- 下面的sql执行时间较长
-- delete from inv_document where not exists(select 1 from inv_document_item where inv_document.id = inv_document_item.inventory_document_id);
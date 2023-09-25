-- 系统初始化
delete from sys_role where id <> 694587732420202496;
delete from sys_role_permission where role_id <> 694587732420202496;
delete from sys_user where id <> 1;
delete from sys_user_role where user_id <> 1;
# delete from core_material_category where id not in (719894871824945152, 719894910232186880, 719894937591631872, 719895017157578752);
update sys_dict set remark = '0' where type = 'MATERIAL_TYPE' AND name = 'ROH';
/* update sys_dict set remark = '10000' where type = 'MATERIAL_TYPE' AND name = 'HALB';
update sys_dict set remark = '20000' where type = 'MATERIAL_TYPE' AND name = 'FERT';
update sys_dict set remark = '30000' where type = 'MATERIAL_TYPE' AND name = 'HIBE'; */

delete from mm_classification where exists(select 1 from mm_material where material_type = 'ROH' and id = mm_classification.material_id);
delete from `mm_characteristic_value` where exists(select 1 from mm_profile where exists(select 1 from mm_material where material_type = 'ROH' and id = mm_profile.material_id) AND mm_characteristic_value.`reference_id` = mm_profile.id);
delete from mm_profile where exists(select 1 from mm_material where material_type = 'ROH' and id = mm_profile.material_id);
delete from mm_material where material_type = 'ROH';

truncate table core_partner;

delete from produce_bom_template_component_detail where type = 'MATERIAL' AND exists (select 1 from mm_material where material_type = 'ROH' and id = produce_bom_template_component_detail.`type_instance_id`);

-- 以下是非 sys_开头的表，业务数据
truncate table pur_source_list;
truncate table sys_document;

update mm_material set standard_price = null;
truncate table inv_document;
truncate table inv_document_item;
truncate table inv_stock;

truncate table mm_batch;
delete from mm_profile where category = 'BATCH';
truncate table mm_characteristic_value;

truncate table produce_order;
truncate table produce_order_item;
truncate table produce_order_item_detail;

truncate table pur_purchase_order;
truncate table pur_purchase_order_item;
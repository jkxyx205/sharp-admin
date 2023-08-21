truncate table sys_document;

delete from `sys_form_configurer` where id NOT IN (select config_id from sys_form_cpn_configurer) AND name <> 'COLOR';
delete from sys_role where id <> 694587732420202496;
delete from sys_role_permission where role_id <> 694587732420202496;
delete from sys_user where id <> 1;
delete from sys_user_role where user_id <> 1;
# delete from core_material_category where id <> 1;
update sys_dict set remark = '0' where type = 'MATERIAL_TYPE' AND name = 'ROH';
update sys_dict set remark = '10000' where type = 'MATERIAL_TYPE' AND name = 'HALB';
update sys_dict set remark = '20000' where type = 'MATERIAL_TYPE' AND name = 'FERT';
update sys_dict set remark = '30000' where type = 'MATERIAL_TYPE' AND name = 'HIBE';

delete from pur_source_list where not exists(select 1 from core_partner where `partner_type` = 'VENDOR' AND core_partner.id = pur_source_list.partner_id)

-- 以下是非 sys_开头的表
truncate table inv_document;
truncate table inv_document_item;
truncate table inv_stock;

truncate table mm_batch;
truncate table mm_profile;
truncate table mm_characteristic_value;

truncate table produce_order;
truncate table produce_order_item;
truncate table produce_order_item_detail;

truncate table pur_purchase_order;
truncate table pur_purchase_order_item;




package com.rick.admin.module.core.dao;

import com.rick.admin.module.core.entity.Plant;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;

/**
 * @author Rick.Xu
 * @date 2023/6/1 11:13
 */
//@Repository
// 自动扫描实现，手动继承 EntityCodeDAOImpl
@Deprecated
public class PlantDAO extends EntityCodeDAOImpl<Plant, Long> {

}
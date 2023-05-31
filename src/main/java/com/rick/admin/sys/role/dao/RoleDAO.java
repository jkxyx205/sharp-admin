package com.rick.admin.sys.role.dao;

import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.admin.sys.role.entity.Role;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2023/5/28 00:18
 */
@Repository
public class RoleDAO extends EntityCodeDAOImpl<Role, Long> {

}
package com.rick.admin.sys.user.dao;

import com.google.common.collect.Lists;
import com.rick.admin.auth.common.AuthConstants;
import com.rick.admin.sys.role.entity.Role;
import com.rick.common.util.JsonUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.admin.sys.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:01
 */
@Repository
@RequiredArgsConstructor
public class UserDAO extends EntityCodeDAOImpl<User, Long> {

    private final PasswordEncoder passwordEncoder;

    @Override
    public int insertOrUpdate(Map<String, Object> params) {
        params.put("password", passwordEncoder.encode(AuthConstants.DEFAULT_PASSWORD));

        String roleIds = (String) params.get("roleIds");
        List<String> roleIdList = JsonUtils.toList(roleIds, String.class);
        List<Role> roleList = Lists.newArrayListWithExpectedSize(roleIdList.size());
        for (String id : roleIdList) {
            roleList.add(Role.builder().id(Long.parseLong(id)).build());
        }
        params.put("roleList", roleList);

        return super.insertOrUpdate(params);
    }

    @Override
    public int update(User entity) {
        return super.update(entity);
    }
}
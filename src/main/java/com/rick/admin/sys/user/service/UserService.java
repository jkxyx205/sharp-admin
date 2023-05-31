package com.rick.admin.sys.user.service;

import com.rick.admin.common.ExceptionCodeEnum;
import com.rick.admin.sys.permission.dao.PermissionDAO;
import com.rick.admin.sys.permission.entity.Permission;
import com.rick.admin.sys.role.dao.RoleDAO;
import com.rick.admin.sys.role.entity.Role;
import com.rick.admin.sys.user.dao.UserDAO;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.http.exception.BizException;
import com.rick.db.service.support.Params;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:04
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class UserService {

    PasswordEncoder passwordEncoder;

    UserDAO userDAO;

    public Optional<User> findByUsername(String username) {
        Optional<User> optional = userDAO.selectByCode(username);
        if (optional.isPresent()) {
            User user = optional.get();
            if (!user.getAvailable()) {
                throw new BizException(ExceptionCodeEnum.USER_NOT_AVAILABLE);
            }

            List<String> authorityList = new ArrayList<>();

            for (Role role : user.getRoleList()) {
                authorityList.add("ROLE_" + role.getCode());
                for (Permission permission : role.getPermissionList()) {
                    authorityList.add(permission.getCode());
                }
            }

            user.setAuthorityList(authorityList);;
        }

        return optional;
    }

    public boolean checkPassword(Long userId, String password) {
        return passwordEncoder.matches(password, userDAO.selectSingleValueById(userId, "password", String.class).get());
    }

    public void updatePassword(Long userId, String password) {
        userDAO.update("password", Params.builder(2).pv("password", passwordEncoder.encode(password)).pv("id", userId).build(), "id = :id");
    }
}
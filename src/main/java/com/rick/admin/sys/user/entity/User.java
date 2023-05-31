package com.rick.admin.sys.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rick.admin.sys.role.entity.Role;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:45
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "sys_user", comment = "用户信息")
public class User extends BaseCodeEntity {

    @Column(comment = "姓名")
    String name;

    @Column(comment = "密码", columnDefinition = "varchar(128)", updatable = false)
    String password;

    @Column(value = "is_available", comment = "是否可用")
    Boolean available;


    @ManyToMany(thirdPartyTable="sys_user_role", referenceTable="sys_role", referenceColumnName = "role_id", columnDefinition = "user_id")
    List<Role> roleList;

    @Transient
    List<Long> roleIds;

    /**
     * 权限列表
     */
    @Transient
    List<String> authorityList;

    @Transient
    private String imgName;

    public List<String> getRoleIds() {
        if (roleList == null) {
            return Collections.emptyList();
        }

        return roleList.stream().map(role -> String.valueOf(role.getId())).collect(Collectors.toList());
    }
}
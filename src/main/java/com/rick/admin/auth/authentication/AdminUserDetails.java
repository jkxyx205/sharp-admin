package com.rick.admin.auth.authentication;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/10/19 5:16 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Getter
public class AdminUserDetails extends User {

    private com.rick.admin.sys.user.entity.User user;

    public AdminUserDetails(com.rick.admin.sys.user.entity.User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getCode(), user.getPassword(), authorities);
        this.user = user;
    }
}

package com.rick.admin.auth.exception;


import org.springframework.security.core.AuthenticationException;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 12/18/19 9:58 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public class MaxTryLoginException extends AuthenticationException {

    public MaxTryLoginException(String message) {
        super(message);
    }
}

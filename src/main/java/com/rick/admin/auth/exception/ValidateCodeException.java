package com.rick.admin.auth.exception;

import com.rick.admin.common.ExceptionCodeEnum;
import org.springframework.security.core.AuthenticationException;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 验证码错误异常
 * @author: Rick.Xu
 * @date: 12/19/19 11:13 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public class ValidateCodeException extends AuthenticationException {

    public ValidateCodeException() {
        super(ExceptionCodeEnum.VALIDATE_CODE_ERROR.getMessage());
    }
}

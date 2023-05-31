package com.rick.admin.common;

import com.rick.common.http.exception.ExceptionCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:46
 */
@Getter
@ToString
public enum ExceptionCodeEnum implements ExceptionCode {
    USER_NOT_AVAILABLE(400010, "用户状态不可用，请联系管理员"),
    INVALID_SESSION(400011, "访问超时，请退出后重新登录"),
    VALIDATE_CODE_ERROR(40003, "验证码输入不正确"),
    ROLE_NULL_ERROR(404009, "角色名不能为空");

    private int code;
    private String message;
    ExceptionCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

}

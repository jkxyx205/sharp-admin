package com.rick.admin.auth.validate.image;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 12/19/19 10:48 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Data
public class CaptchaImageVO {
    //验证码文字
    private String code;
    //验证码失效时间
    private LocalDateTime expireTime;

    public CaptchaImageVO(String code, int expireAfterSeconds){
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expireAfterSeconds);
    }

    //验证码是否失效
    public boolean isExpried() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}

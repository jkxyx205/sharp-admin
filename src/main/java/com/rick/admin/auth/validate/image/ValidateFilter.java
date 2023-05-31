package com.rick.admin.auth.validate.image;

import com.rick.admin.auth.exception.ValidateCodeException;
import com.rick.admin.common.AdminConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 图片验证码
 * @author: Rick.Xu
 * @date: 12/19/19 11:07 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Component("validateFilter")
public class ValidateFilter extends OncePerRequestFilter implements InitializingBean {

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uiCode = request.getParameter("code");
        CaptchaImageVO captchaImageVO = (CaptchaImageVO) request.getSession().getAttribute(AdminConstants.KAPTCHA_SESSION_KEY);

        if ("/login".equals(request.getServletPath()) && request.getMethod().equals("POST")) {
//            if (Objects.isNull(captchaImageVO) || captchaImageVO.isExpried() || !StringUtils.equals(uiCode, captchaImageVO.getCode())) {
            if (Objects.nonNull(captchaImageVO) && (captchaImageVO.isExpried() || !StringUtils.equals(uiCode, captchaImageVO.getCode()))) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, new ValidateCodeException());
                return;
            } else {
                request.getSession().removeAttribute(AdminConstants.KAPTCHA_SESSION_KEY);
            }
        }

        // 别忘了这个..
        filterChain.doFilter(request, response);
    }
}

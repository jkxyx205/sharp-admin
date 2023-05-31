package com.rick.admin.auth.authentication;

import com.rick.admin.auth.common.AuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/10/19 4:29 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Component
@RequiredArgsConstructor
public class AdminAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final CacheManager cacheManager;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws ServletException, IOException {
        Cache tryCache = cacheManager.getCache("loginMaxTry");

        Object object = tryCache.get(request.getParameter("username").toUpperCase());

        if (Objects.nonNull(object)) {
            // 验证码失败，还没有用户名
            long loginMaxTryCount = (Integer) object;

            if (loginMaxTryCount > AuthConstants.MAX_TRY_IMAGE_CODE_COUNT) {
                request.getSession().setAttribute(AuthConstants.IMAGE_CODE_SESSION_KEY, true);
            }
        }

        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, e);
//        request.getRequestDispatcher("/login").forward(request, response); //ERROR c.i.ac.exception.AcExceptionHandler - org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'POST' not supported

        response.sendRedirect("/login");
    }
}

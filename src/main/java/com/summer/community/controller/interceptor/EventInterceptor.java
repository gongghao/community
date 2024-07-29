package com.summer.community.controller.interceptor;

import com.summer.community.util.CookieUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-29-15:44
 */
@Deprecated
@Component
public class EventInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String role = CookieUtil.getValue(request, "role");

        if(role == null){
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        else{
            String path = request.getRequestURI();
            return true;
        }
    }
}

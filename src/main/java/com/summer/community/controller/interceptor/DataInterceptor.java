package com.summer.community.controller.interceptor;

import com.summer.community.entity.User;
import com.summer.community.service.DataService;
import com.summer.community.util.CookieUtil;
import com.summer.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-29-15:44
 */
@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteHost();
        if(ip.equals("0:0:0:0:0:0:0:1"))
            ip = "127.0.0.1";
        dataService.recordUV(ip);

        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}

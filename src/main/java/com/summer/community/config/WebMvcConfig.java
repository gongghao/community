package com.summer.community.config;

import com.summer.community.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-30-12:00d
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private EventInterceptor eventInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpeg", "/**/*.jpg")
                .addPathPatterns("/register", "/login");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpeg", "/**/*.jpg");
//
//        registry.addInterceptor(eventInterceptor)
//                .addPathPatterns("/discuss/delete", "/discuss/wonderful", "/discuss/top", "/comment/add/*");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpeg", "/**/*.jpg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpeg", "/**/*.jpg");
    }
}

package com.summer.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-30-17:51
 */
public class CookieUtil {

    public static String getValue(HttpServletRequest request, String name) {
        if (request == null || name == null)
            throw new IllegalArgumentException("Argument is empty!");

        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals(name))
                    return cookie.getValue();

        return null;
    }
}

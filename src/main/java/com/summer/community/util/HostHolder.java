package com.summer.community.util;

import com.summer.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-30-19:23
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}

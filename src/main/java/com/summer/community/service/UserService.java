package com.summer.community.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summer.community.entity.LoginTicket;
import com.summer.community.entity.User;
import com.summer.community.mapper.LoginTickerMapper;
import com.summer.community.mapper.UserMapper;
import com.summer.community.util.CommunityConstant;
import com.summer.community.util.CommunityUtil;
import com.summer.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-14:24
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTickerMapper loginTickerMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int userId) {
        return userMapper.selectById(userId);
    }

    private User selectByName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    private User selectByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userMapper.selectOne(queryWrapper);
    }

    public LoginTicket findLoginTicket(String ticket) {
        QueryWrapper<LoginTicket> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ticket", ticket);
        return loginTickerMapper.selectOne(queryWrapper);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //deal with empty
        if (user == null)
            throw new IllegalArgumentException("Param is Empty!");
        else if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "username is Empty!");
            return map;
        } else if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "password is Empty!");
            return map;
        } else if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "email is Empty!");
            return map;
        }

        // validate user
        User user_in_db = selectByName(user.getUsername());
        if (user_in_db != null) {
            map.put("usernameMsg", "User Exists!");
            return map;
        }

        // validata mail
        user_in_db = selectByEmail(user.getEmail());
        if (user_in_db != null) {
            map.put("emailMsg", "Email has been registered!");
            return map;
        }

        // register user
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(
                String.format("http://images.nowcoder.com/header/%dt.png",
                        new Random().nextInt(1000)
                )
        );
        user.setCreateTime(new Date());
        userMapper.insert(user);

        // send mail
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "Activation", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        // been activated
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            user.setStatus(1);
            userMapper.updateById(user);
            return ACTIVATION_SUCCESS;
        } else return ACTIVATION_FAILURE;
    }

    public Map<String, Object> login(String username, String password, int expired) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "Account can not be empty");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "Password can not be empty");
            return map;
        }

        // validate account
        User user = selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "Account dose not exist");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "Account is not been activated");
            return map;
        }

        // validate password
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "Password error");
        }

        // generate login ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expired * 1000));

        loginTickerMapper.insert(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        QueryWrapper<LoginTicket> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ticket", ticket);
        LoginTicket loginTicket = loginTickerMapper.selectOne(queryWrapper);
        loginTicket.setStatus(1);
        loginTickerMapper.update(loginTicket, queryWrapper);
    }

    public int updateHeader(int userId, String headerUrl){
        User user = userMapper.selectById(userId);
        user.setHeaderUrl(headerUrl);
        return userMapper.updateById(user);
    }
}

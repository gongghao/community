package com.summer.community.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summer.community.entity.LoginTicket;
import com.summer.community.entity.User;
import com.summer.community.mapper.LoginTickerMapper;
import com.summer.community.mapper.UserMapper;
import com.summer.community.util.CommunityConstant;
import com.summer.community.util.CommunityUtil;
import com.summer.community.util.MailClient;
import com.summer.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-14:24
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTickerMapper loginTickerMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int userId) {
//        return userMapper.selectById(userId);
        User user = getCache(userId);
        if (user == null)
            user = initCache(userId);
        return user;
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
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
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
            clearCache(userId);
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

//        loginTickerMapper.insert(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);


        map.put("ticket", loginTicket.getTicket());
        map.put("role", user.getType());
        return map;
    }

    public void logout(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public int updateHeader(int userId, String headerUrl) {
        User user = userMapper.selectById(userId);
        user.setHeaderUrl(headerUrl);
        int rows = userMapper.updateById(user);
        clearCache(userId);
        return rows;
    }

    public Map<String, Object> changePassword(User user, String old_password, String new_password) {
        Map<String, Object> map = new HashMap<>();
        //deal with empty
        if (user == null)
            throw new IllegalArgumentException("Param is Empty!");
        else if (StringUtils.isBlank(old_password)) {
            map.put("oldMsg", "原密码为空");
            return map;
        } else if (StringUtils.isBlank(new_password)) {
            map.put("newMsg", "新密码为空");
            return map;
        }

        // validate user
        String password = CommunityUtil.md5(old_password + user.getSalt());
        if (!password.equals(user.getPassword())) {
            map.put("oldMsg", "password error");
            return map;
        }
        if (new_password.equals(old_password)) {
            map.put("newMsg", "two passwords cannot be the same!");
            return map;
        }

        // change password
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(new_password + user.getSalt()));
        userMapper.updateById(user);
        clearCache(user.getId());

        return map;
    }

    public Map<String, Object> changeUsername(User user, String username) {
        Map<String, Object> map = new HashMap<>();
        //deal with empty
        if (user == null)
            throw new IllegalArgumentException("Param is Empty!");
        else if (StringUtils.isBlank(username)) {
            map.put("Msg", "昵称为空！");
            return map;
        }
        else if (user.getUsername().equals(username)) {
            map.put("Msg", "昵称不能与原昵称相同！");
            return map;
        }

        // validate user
        User findUser = findUserByName(username);
        if (findUser != null) {
            map.put("Msg", "该昵称已被占用！");
            return map;
        }

        // change username
        user.setUsername(username);
        userMapper.updateById(user);
        clearCache(user.getId());

        return map;
    }

    public User findUserByName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    // 优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 取不到值时初始化缓存数据
    private User initCache(int UserId) {
        User user = userMapper.selectById(UserId);
        String redisKey = RedisKeyUtil.getUserKey(user.getId());
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 当数据变更时，清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}

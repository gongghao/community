package com.summer.community.controller;

import com.google.code.kaptcha.Producer;
import com.summer.community.entity.Result;
import com.summer.community.entity.User;
import com.summer.community.service.UserService;
import com.summer.community.util.CommunityConstant;
import com.summer.community.util.CommunityUtil;
import com.summer.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-16:45
 */
@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    @ResponseBody
    public String register(User user) {
        Result result = Result.ok("/register.post");

        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            //model.addAttribute("msg", "Register Success, email has been sent to you.");
            //model.addAttribute("target", "/index");
            result.data("msg", "Register Success, email has been sent to you.");
            result.data("target", "/index");
            //return "/site/operate-result";
            return result.toString();
        } else {
            //model.addAttribute("usernameMsg", map.get("usernameMsg"));
            //model.addAttribute("passwordMsg", map.get("passwordMsg"));
            //model.addAttribute("emailMsg", map.get("emailMsg"));
            result.data("usernameMsg", map.get("usernameMsg"));
            result.data("passwordMsg", map.get("passwordMsg"));
            result.data("emailMsg", map.get("emailMsg"));
            result.data("target", "/site/register");
            //return "/site/register";
            return result.toString();
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    @ResponseBody
    public String activation(@PathVariable("userId") int userId, @PathVariable("code") String code) {
        Result result = Result.ok("/activation/{userId}/{code}.get");

        int end = userService.activation(userId, code);
        if (end == ACTIVATION_SUCCESS) {
            //model.addAttribute("msg", "Activate successfully, get starting!");
            //model.addAttribute("target", "/login");
            result.data("meg", "Activate successfully, get starting!");
            result.data("target", "/login");
        } else if (end == ACTIVATION_REPEAT) {
           // model.addAttribute("msg", "Your account has be activated.");
            //model.addAttribute("target", "/index");
            result.data("meg", "Your account had been activated.");
            result.data("target", "/index");
        } else {
            //model.addAttribute("msg", "Activate failed, your activation code is wrong.");
            //model.addAttribute("target", "/index");
            result.data("msg", "Activate failed, your activation code is wrong.");
            result.data("target", "/index");
        }
        //return "/site/operate-result";
        return result.toString();
    }

//    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
//    public void getKaptcha(HttpServletResponse response, HttpSession session){
//        String text = kaptchaProducer.createText();
//        BufferedImage image = kaptchaProducer.createImage(text);
//
//        session.setAttribute("kaptcha", text);
//
//        response.setContentType("image/png");
//        try{
//            OutputStream outputStream = response.getOutputStream();
//            ImageIO.write(image, "png", outputStream);
//        }
//        catch (IOException e)
//        {
//            logger.error("响应验证码失败" + e.getMessage());
//        }
//    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response) {

        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片传给浏览器
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败", e.getMessage());
        }
    }

//    @RequestMapping(path = "/login", method = RequestMethod.POST)
//    public String login(String username, String password, String code,
//                        boolean rememberme, Model model, HttpSession session, HttpServletResponse response) {
//        String kaptcha = (String) session.getAttribute("kaptcha");
//        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code))
//        {
//            model.addAttribute("codeMsg", "验证码错误!");
//            return "/site/login";
//        }
//
//        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
//
//        Map<String, Object> map = userService.login(username, password, expiredSeconds);
//        if(map.containsKey("ticket")){
//            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
//            cookie.setPath(contextPath);
//            cookie.setMaxAge(expiredSeconds);
//            response.addCookie(cookie);
//            return "redirect:/index";
//        }else{
//            model.addAttribute("usernameMsg", map.get("usernameMsg"));
//            model.addAttribute("passwordMsg", map.get("passwordMsg"));
//            return "/site/login";
//        }
//    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(String username, String password, String code,
                        boolean rememberme, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        Result result = Result.ok("/login.post");

        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            //model.addAttribute("codeMsg", "验证码错误!");
            result.data("codeMsg", "验证码错误！");
            //return "/site/login";
            result.data("target", "/site//login");
            return result.toString();
        }

        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;

        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            Cookie cookie1 = new Cookie("role", map.get("role").toString());
            cookie1.setPath(contextPath);
            cookie1.setMaxAge(expiredSeconds);
            response.addCookie(cookie1);
            response.addCookie(cookie);

            //return "redirect:/index";
            result.data("target", "/index");
            return result.toString();
        } else {
            //model.addAttribute("usernameMsg", map.get("usernameMsg"));
            //model.addAttribute("passwordMsg", map.get("passwordMsg"));
            result.data("usernameMsg", map.get("usernameMsg"));
            result.data("passwordMsg", map.get("passwordMsg"));
            //return "/site/login";
            result.data("target", "/site/login");
            return result.toString();
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public String logout(@CookieValue("ticket") String ticket) {
        Result result = Result.ok("/logout.get");
        userService.logout(ticket);
        //return "redirect:/login";
        result.data("target", "/login");
        return result.toString();
    }
}

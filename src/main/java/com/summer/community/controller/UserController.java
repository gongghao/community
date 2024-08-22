package com.summer.community.controller;

import com.summer.community.annotation.LoginRequired;
import com.summer.community.entity.Result;
import com.summer.community.entity.User;
import com.summer.community.mapper.DiscussPostMapper;
import com.summer.community.mapper.UserMapper;
import com.summer.community.service.FollowService;
import com.summer.community.service.LikeService;
import com.summer.community.service.UserService;
import com.summer.community.util.CommunityConstant;
import com.summer.community.util.CommunityUtil;
import com.summer.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-12:44
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadHeader(MultipartFile headerImage) {
        Result result = Result.ok("/upload.post");

        if (headerImage == null) {
//            model.addAttribute("error", "您还未选择图片");
            //return "/site/setting";
            result.setSuccess(false);
            result.setMessage("您还未选择图片!");
            return result.toString();
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            //model.addAttribute("error", "文件格式错误");
            //return "/site/setting";
            result.setSuccess(false);
            result.setMessage("文件格式错误!");
            return result.toString();
        }

        filename = CommunityUtil.generateUUID() + suffix;
        File file = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败", e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常");
        }

        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        result.data("target", "/index");
        return result.toString();
        //return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("/image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }
    }

    @RequestMapping(path = "/changePassword", method = RequestMethod.POST)
    @ResponseBody
    public String changePassword(String old_password, String new_password) {
        Result result = Result.ok("/changePassword.post");

        User user = hostHolder.getUser();
        Map<String, Object> map = userService.changePassword(user, old_password, new_password);
        if (map == null || map.isEmpty()) {
            //model.addAttribute("msg", "Password changed successfully!");
            //return "redirect:/index";
            result.data("msg", "Password changed successfully!");
            result.data("target", "/index");
            return result.toString();

        } else {
            //model.addAttribute("oldMsg", map.get("oldMsg"));
            //model.addAttribute("newMsg", map.get("newMsg"));
            //return "/site/setting";
            result.data("oldMsg", map.get("oldMsg"));
            result.data("newMsg", map.get("newMsg"));
            result.data("target", "/site/setting");
            return result.toString();
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public String getProfilePage(@PathVariable("userId") int userId) {
        Result result = Result.ok("/profile/{userId}.get");

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 用户
        //model.addAttribute("user", user);
        //model.addAttribute("loginUser", hostHolder.getUser());
        result.data("user", user);
        result.data("loginUser", hostHolder.getUser());
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        //model.addAttribute("likeCount", likeCount);
        result.data("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        //model.addAttribute("followeeCount", followeeCount);
        result.data("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        //model.addAttribute("followerCount", followerCount);
        result.data("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null)
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        //model.addAttribute("hasFollowed", hasFollowed);
        result.data("hasFollowed", hasFollowed);

        //return "/site/profile";
        return result.toString();
    }
}

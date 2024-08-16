package com.summer.community.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.summer.community.entity.Event;
import com.summer.community.entity.Page;
import com.summer.community.entity.Result;
import com.summer.community.entity.User;
import com.summer.community.event.EventProducer;
import com.summer.community.service.FollowService;
import com.summer.community.service.UserService;
import com.summer.community.util.CommunityConstant;
import com.summer.community.util.CommunityUtil;
import com.summer.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-12-16:13
 */
@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        Result result = Result.ok("/follow.post");

        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        result.setSuccess(true);
        result.setCode(0);
        result.setMessage("已关注");
        return result.toString();
        //return CommunityUtil.getJSONString(0, "已关注！");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        Result result = Result.ok("unfollow.post");

        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        result.setSuccess(true);
        result.setCode(0);
        result.setMessage("已取消关注");
        return result.toString();
        //return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public String getFollowees(@PathVariable("userId") int userId, Model model) {
        Result result = Result.ok("/followees/{userId}.get");
        Page page = new Page();

        User user = userService.findUserById(userId);
        if (user == null)
            throw new RuntimeException("该用户不存在!");
        //model.addAttribute("user", user);
        result.data("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null)
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        //model.addAttribute("users", userList);
        //model.addAttribute("loginUser", hostHolder.getUser());
        result.data("users", userList);
        result.data("loginUser", hostHolder.getUser());

        //return "/site/followee";
        return result.toString();
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public String getFollowers(@PathVariable("userId") int userId, Model model) {
        Result result = Result.ok("/followers/{userId}.get");
        Page page = new Page();

        User user = userService.findUserById(userId);
        if (user == null)
            throw new RuntimeException("该用户不存在!");
        //model.addAttribute("user", user);
        result.data("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null)
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        //model.addAttribute("users", userList);
        //model.addAttribute("loginUser", hostHolder.getUser());
        result.data("users", userList);
        result.data("loginUser", hostHolder.getUser());

        //return "/site/follower";
        return result.toString();
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null)
            return false;

        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}

package com.summer.community.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.summer.community.entity.*;
import com.summer.community.event.EventProducer;
import com.summer.community.service.CommentService;
import com.summer.community.service.DiscussPostService;
import com.summer.community.service.LikeService;
import com.summer.community.service.UserService;
import com.summer.community.util.CommunityUtil;
import com.summer.community.util.HostHolder;
import com.summer.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.summer.community.util.CommunityConstant.*;
import static java.lang.Integer.MAX_VALUE;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-03-20:36
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        Result result = Result.ok("/add.post");
        User user = hostHolder.getUser();
        if (user == null) {
           // return CommunityUtil.getJSONString(403, "用户未登录");
            result.setSuccess(false);
            result.setCode(403);
            result.setMessage("用户未登录");
            return result.toString();
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错的情况，将来统一处理
        result.setSuccess(true);
        result.setCode(0);
        result.setMessage("发布成功!");
      //  return CommunityUtil.getJSONString(0, "发布成功!");
        return result.toString();
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    @ResponseBody
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model) {
        Result result = Result.ok("/detail/{discussPostId}.get");
        Page page = new Page();
        // 帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        //model.addAttribute("post", discussPost);
        result.data("post", discussPost);
        // 作者
        User user = userService.findUserById(discussPost.getUserId());
        //model.addAttribute("user", user);
        result.data("user", user);
        // 点赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        //model.addAttribute("likeCount", likeCount);
        result.data("likeCount", likeCount);
        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        //model.addAttribute("likeStatus", likeStatus);
        result.data("likeStatus", likeStatus);
        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());

        // 评论：给帖子的评论
        // 回复：给评论的评论
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
        // 显示的对象
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null)
            for (Comment comment : commentList) {
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, MAX_VALUE);
                // 回复Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null)
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(comment.getId(), ENTITY_TYPE_COMMENT);
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }

        //model.addAttribute("comments", commentVoList);
        result.data("comments", commentVoList);

        result.data("Page", page);
        return result.toString();
    }

    // 置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        Result result = Result.ok("/top.post");

        User user = hostHolder.getUser();
//        System.out.println(user.getUsername());
//        System.out.println(user.getType());
//        for(int i = 0; i< 10; i++)
//            System.out.println("");

        if (user == null) {
           // return CommunityUtil.getJSONString(403, "用户未登录");
            result.setSuccess(false);
            result.setCode(403);
            result.setMessage("用户未登录");
            return result.toString();
        }
        else if (user.getType() != 2) {
            //return CommunityUtil.getJSONString(401, "权限不足");
            result.setSuccess(false);
            result.setCode(401);
            result.setMessage("权限不足");
            return result.toString();
        }

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        discussPostService.updateType(id, 1);

        result.setSuccess(true);
        result.setCode(0);
        result.setMessage("成功");
        return result.toString();
       // return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        Result result = Result.ok("/wonderful.post");

        User user = hostHolder.getUser();

        if (user == null) {
            // return CommunityUtil.getJSONString(403, "用户未登录");
            result.setSuccess(false);
            result.setCode(403);
            result.setMessage("用户未登录");
            return result.toString();
        }
        else if (user.getType() != 2) {
            //return CommunityUtil.getJSONString(401, "权限不足");
            result.setSuccess(false);
            result.setCode(401);
            result.setMessage("权限不足");
            return result.toString();
        }

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        discussPostService.updateStatus(id, 1);

        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        //return CommunityUtil.getJSONString(0);
        result.setSuccess(true);
        result.setCode(0);
        result.setMessage("成功");
        return result.toString();
    }

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        Result result = Result.ok("/delete.post");

        User user = hostHolder.getUser();

        if (user == null) {
            // return CommunityUtil.getJSONString(403, "用户未登录");
            result.setSuccess(false);
            result.setCode(403);
            result.setMessage("用户未登录");
            return result.toString();
        }
        else if (user.getType() != 1) {
            //return CommunityUtil.getJSONString(401, "权限不足");
            result.setSuccess(false);
            result.setCode(401);
            result.setMessage("权限不足");
            return result.toString();
        }

        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        discussPostService.updateStatus(id, 2);

        //return CommunityUtil.getJSONString(0);
        result.setSuccess(true);
        result.setCode(0);
        result.setMessage("成功");
        return result.toString();
    }
}

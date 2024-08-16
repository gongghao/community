package com.summer.community.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.summer.community.entity.Event;
import com.summer.community.entity.Result;
import com.summer.community.entity.User;
import com.summer.community.event.EventProducer;
import com.summer.community.service.LikeService;
import com.summer.community.util.CommunityConstant;
import com.summer.community.util.CommunityUtil;
import com.summer.community.util.HostHolder;
import com.summer.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-11-15:00
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        Result result = Result.ok("/like.post");

        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

//        Map<String, Object> map = new HashMap<>();
//        map.put("likeCount", likeCount);
//        map.put("likeStatus", likeStatus);
        result.data("likeCount", likeCount);
        result.data("likeStatus", likeStatus);

        // 触发点赞事件
        if(likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        if (entityType == ENTITY_TYPE_POST) {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        //return CommunityUtil.getJSONString(0, null, map);
        return result.toString();
    }

}

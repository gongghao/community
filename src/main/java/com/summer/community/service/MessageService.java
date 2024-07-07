package com.summer.community.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summer.community.entity.Message;
import com.summer.community.mapper.MessageMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-07-13:42
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信
    public List<Message> findConversations(int userId, int offset, int limit) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(id)");
        queryWrapper.ne("status", 2);
        queryWrapper.ne("from_id", 1);
        queryWrapper.eq("from_id", userId).or().eq("to_id", userId);
        queryWrapper.groupBy("conversation_id");
        List<Map<String, Object>> mapList = messageMapper.selectMaps(queryWrapper);

        List<Integer> ids = new ArrayList<>();
        for (Map<String, Object> map : mapList)
            ids.add((Integer) map.get("max(id)"));

        queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit " + String.valueOf(offset) + ", " + String.valueOf(limit));
        return messageMapper.selectList(queryWrapper);
    }

    // 查询当前用户的会话数量
    public int findConversationCount(int userId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(id)");
        queryWrapper.ne("status", 2);
        queryWrapper.ne("from_id", 1);
        queryWrapper.eq("from_id", userId).or().eq("to_id", userId);
        queryWrapper.groupBy("conversation_id");
        List<Map<String, Object>> mapList = messageMapper.selectMaps(queryWrapper);
        return mapList.size();
    }

    // 查询某个会话所包含的私信列表
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2);
        queryWrapper.ne("from_id", 1);
        queryWrapper.eq("conversation_id", conversationId);
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit " + String.valueOf(offset) + ", " + String.valueOf(limit));
        return messageMapper.selectList(queryWrapper);
    }

    // 查询某个会话所包含的私信数量
    public int findLetterCount(String conversationId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2);
        queryWrapper.ne("from_id", 1);
        queryWrapper.eq("conversation_id", conversationId);
        return messageMapper.selectCount(queryWrapper);
    }

    // 查询未读私信的数量
    public int findLetterUnreadCount(int userId, String conversationId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0);
        queryWrapper.ne("from_id", 1);
        queryWrapper.eq("to_id", userId);
        if (conversationId != null && StringUtils.isBlank(conversationId))
            queryWrapper.eq("conversation_id", conversationId);
        return messageMapper.selectCount(queryWrapper);
    }


}

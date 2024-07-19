package com.summer.community.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summer.community.entity.Message;
import com.summer.community.mapper.MessageMapper;
import com.summer.community.util.SensitiveFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

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

    @Autowired
    private SensitiveFilter sensitiveFilter;

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
        queryWrapper.ne("from_id", 1);
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit " + String.valueOf(offset) + ", " + String.valueOf(limit));
        return messageMapper.selectList(queryWrapper);
    }

    // 查询当前用户的会话数量
    public int findConversationCount(int userId) {
        return findConversations(userId, 0, 32768).size();
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

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insert(message);
    }

    //修改消息状态
    public void updateStatus(List<Integer> ids, int status) {
        for (int id : ids) {
            Message message = messageMapper.selectById(id);
            message.setStatus(status);
            messageMapper.updateById(message);
        }
    }

    public void readMessage(List<Integer> ids) {
        updateStatus(ids, 1);
    }

    // 查询某个主题下最新的通知
    public Message findLatestNotice(int userId, String topic) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2);
        queryWrapper.eq("from_id", 1);
        queryWrapper.eq("to_id", userId);
        queryWrapper.eq("conversation_id", topic);
        queryWrapper.orderByDesc("id");

        List<Message> list = messageMapper.selectList(queryWrapper);
        if (list.isEmpty())
            return null;
        return list.get(0);

    }

    // 查询某个主题所包含的通知的数量
    public int findNoticeCount(int userId, String topic) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2);
        queryWrapper.eq("from_id", 1);
        queryWrapper.eq("to_id", userId);
        queryWrapper.eq("conversation_id", topic);
        return messageMapper.selectCount(queryWrapper);
    }

    // 查询未读的通知的数量
    public int findNoticeUnreadCount(int userId, String topic) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0);
        queryWrapper.eq("from_id", 1);
        queryWrapper.eq("to_id", userId);
        if (topic != null && StringUtils.isNotBlank(topic))
            queryWrapper.eq("conversation_id", topic);
        return messageMapper.selectCount(queryWrapper);
    }

    // 查询某个主题所包含的通知列表
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        QueryWrapper<Message> queryWrapper  = new QueryWrapper();
        queryWrapper.ne("status", 2);
        queryWrapper.eq("from_id", 1);
        queryWrapper.eq("to_id" ,userId);
        queryWrapper.eq("conversation_id", topic);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("limit " + String.valueOf(offset) + ", " + String.valueOf(limit));
        return messageMapper.selectList(queryWrapper);
    }
}

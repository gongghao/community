package com.summer.community.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summer.community.entity.Comment;
import com.summer.community.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-05-13:38
 */
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("status", 0);
        queryWrapper.eq("entity_type", entityType);
        queryWrapper.eq("entity_id", entityId);
        queryWrapper.orderByAsc("create_time");
        queryWrapper.last("limit " + String.valueOf(offset) + ", " + String.valueOf(limit));
        return commentMapper.selectList(queryWrapper);
    }

    public int findCommentCount(int entityId, int entityType) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("status", 0);
        queryWrapper.eq("entity_type", entityType);
        queryWrapper.eq("entity_id", entityId);
        List<Comment> list = commentMapper.selectList(queryWrapper);
        return list.size();
    }
}

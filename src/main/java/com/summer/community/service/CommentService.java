package com.summer.community.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summer.community.entity.Comment;
import com.summer.community.mapper.CommentMapper;
import com.summer.community.util.CommunityConstant;
import com.summer.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.swing.text.html.HTML;
import java.util.List;

import static com.summer.community.util.CommunityConstant.ENTITY_TYPE_POST;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-05-13:38
 */
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

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

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        commentMapper.insert(comment);
        int rows = commentMapper.selectCount(null);

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("entity_type", ENTITY_TYPE_POST);
            queryWrapper.eq("entity_id", comment.getEntityId());
            int count = commentMapper.selectCount(queryWrapper);
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectById(id);
    }
}

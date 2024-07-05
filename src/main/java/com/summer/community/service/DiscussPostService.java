package com.summer.community.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summer.community.entity.DiscussPost;
import com.summer.community.mapper.DiscussPostMapper;
import com.summer.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-13:03
 */
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDisscussPost(int userId, int offset, int limit) {
        QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2);
        if (userId != 0)
            queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.orderByDesc("type");
        queryWrapper.last("limit " + String.valueOf(offset) + ", " + String.valueOf(limit));
        return discussPostMapper.selectList(queryWrapper);
//        if (discussPostList.size() >= offset + limit)
//            discussPostList.subList(offset, offset + limit);
//        else
//            discussPostList.subList(offset, discussPostList.size());
    }

    public int findDiscussPostRows(int userId) {
        QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2);
        if (userId != 0)
            queryWrapper.eq("user_id", userId);
        List<DiscussPost> disscussPostList = discussPostMapper.selectList(queryWrapper);
        return disscussPostList.size();
    }

    public int addDiscussPost(DiscussPost post) {
        if (post == null)
            throw new IllegalArgumentException("参数不能为空!");

        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insert(post);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectById(id);
    }

}
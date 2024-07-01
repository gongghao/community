package com.summer.community.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summer.community.entity.DiscussPost;
import com.summer.community.mapper.DiscussPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<DiscussPost> findDisscussPost(int userId, int offset, int limit) {
        QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", 2).ne("user_id", 0);
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        List<DiscussPost> discussPostList = discussPostMapper.selectList(queryWrapper);
        if(discussPostList.size() >= offset + limit)
            discussPostList.subList(offset, offset + limit);
        else
            discussPostList.subList(offset, discussPostList.size());
        return discussPostList;
    }

    public int findDiscussPostRows(int userId) {
        QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("user_id", 0);
        queryWrapper.eq("user_id", userId);
        List<DiscussPost> disscussPostList = discussPostMapper.selectList(queryWrapper);
        return disscussPostList.size();
    }
}

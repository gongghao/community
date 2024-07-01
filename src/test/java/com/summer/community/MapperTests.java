package com.summer.community;

import com.summer.community.entity.User;
import com.summer.community.mapper.DiscussPostMapper;
import com.summer.community.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-12:25
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser()
    {
        List<User> list = userMapper.selectList(null);
        System.out.println(list);
    }

    @Test
    public void testSelectPosts()
    {

    }

}

package com.summer.community;

import com.summer.community.entity.DiscussPost;
import com.summer.community.service.DiscussPostService;
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
 * @date: 2024-07-05-15:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class PageTests {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void PageTest() {
        List<DiscussPost> list = discussPostService.findDisscussPost(0, 0, 10);
        for(DiscussPost post : list)
            System.out.println(post);
        System.out.println(list.size());
    }
}

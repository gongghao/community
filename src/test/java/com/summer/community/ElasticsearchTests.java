package com.summer.community;

import com.summer.community.dao.elsaticsearch.DiscussPostRepository;
import com.summer.community.entity.DiscussPost;
import com.summer.community.mapper.DiscussPostMapper;
import com.summer.community.service.ElasticsearchService;
import org.elasticsearch.search.profile.SearchProfileQueryPhaseResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-20-14:42
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectById(1));
        discussPostRepository.save(discussPostMapper.selectById(2));
        discussPostRepository.save(discussPostMapper.selectById(3));


    }

    @Test
    public void testSearchByRepository() {
        List<DiscussPost> temp = elasticsearchService.searchDiscussPost("test", 0, 100);
        if (temp != null) {
            System.out.println(temp.size());
            for (DiscussPost post : temp) {
                System.out.println(post.getUserId() + post.getTitle() + post.getContent());
            }
        }
    }
}

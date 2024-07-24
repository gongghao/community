package com.summer.community.dao.elsaticsearch;

import com.summer.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-20-14:40
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
}

package com.summer.community.service;

import com.summer.community.dao.AlphaDao;
import com.summer.community.entity.DiscussPost;
import com.summer.community.entity.User;
import com.summer.community.mapper.DiscussPostMapper;
import com.summer.community.mapper.UserMapper;
import com.summer.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-0:27
 */
@Service
@Scope("singleton") //单个bean实例，默认
//@Scope("prototype") //多个
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public AlphaService()
    {
        System.out.println("实例化AlphaService");
    }


    @PostConstruct
    public void init()
    {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destory()
    {
        System.out.println("销毁AlphaService");
    }

    public String find()
    {
        return alphaDao.select();
    }


    // REQUIRED 支持当前事务（外部事务），如果不存在则创建新事务
    // REQUIRES_NEW 创建新事务，暂停外部事务
    // NESTED 如果当前存在事务（外部事务），则嵌套在该事务中执行（独立的提交和回滚），否则和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1(){
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insert(user);
        // 新增帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("alpha 事物管理");
        discussPost.setContent("测试");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insert(discussPost);

        Integer.valueOf("abc");
        return "ok";
    }

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 新增用户
                User user = new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("alpha@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insert(user);
                // 新增帖子
                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("alpha 事物管理");
                discussPost.setContent("测试");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insert(discussPost);

                Integer.valueOf("abc");
                return "ok";
            }
        });
    }

}

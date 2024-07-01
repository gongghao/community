package com.summer.community.service;

import com.summer.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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

}

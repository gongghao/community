package com.summer.community.dao;

import org.springframework.stereotype.Repository;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-0:15
 */
@Repository("alphaHibernate")
public class AlphaDaoHibernateImpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}

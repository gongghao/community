package com.summer.community.util;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-29-14:05
 */
public interface CommunityConstant {

    // activate successfully
    int ACTIVATION_SUCCESS = 0;

    int ACTIVATION_REPEAT = 1;

    int ACTIVATION_FAILURE = 2;

    int DEFAULT_EXPIRED_SECONDS = 3600 * 24 * 7;

    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_USER = 3;
}

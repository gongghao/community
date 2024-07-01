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

    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
}

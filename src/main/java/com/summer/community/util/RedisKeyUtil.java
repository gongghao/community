package com.summer.community.util;

import javax.swing.text.html.parser.Entity;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-11-14:26
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}

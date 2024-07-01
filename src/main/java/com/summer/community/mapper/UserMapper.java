package com.summer.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summer.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-06-28-12:45
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

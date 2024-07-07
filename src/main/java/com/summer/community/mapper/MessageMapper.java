package com.summer.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summer.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-07-13:41
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}

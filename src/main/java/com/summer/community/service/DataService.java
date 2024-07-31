package com.summer.community.service;

import com.summer.community.util.RedisKeyUtil;
import org.elasticsearch.common.recycler.Recycler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Version: java version 20
 * @Author: Wei Zhou
 * @date: 2024-07-30-14:37
 */
@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //将指定的IP计入UV
    public void recordUV(String ip) {
//        String RedisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
//        redisTemplate.opsForHyperLogLog().add(RedisKey, ip);

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String RedisKey = RedisKeyUtil.getUVKey(df.format(new Date()));

                boolean isMember = operations.opsForSet().isMember(RedisKey, ip);

                operations.multi();

                if (!isMember)
                    operations.opsForSet().add(RedisKey, ip);

                return operations.exec();
            }
        });
    }

    //统计指定日期范围内的UV
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        //整理该日期内的key
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        long res = 0;
        while(!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            res += redisTemplate.opsForSet().size(key);
            calendar.add(Calendar.DATE, 1);
        }

        //合并这些key
//        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
//        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toString());

        return res;
    }

    public void recordDAU(int userId) {
        String RedisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(RedisKey, userId, true);
    }

    //统计指定日期范围内的UV
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        //整理该日期内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        //进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}

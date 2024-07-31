package com.summer.community;

import com.summer.community.service.DataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-31-14:37
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class DataTests {
    @Autowired
    private DataService dataService;

    @Test
    public void test() {
        dataService.recordUV("127.0.0.1");
        dataService.recordUV("127.0.0.1");
        dataService.recordUV("127.0.0.1");
        dataService.recordUV("127.6.0.1");
        dataService.recordUV("127.9.0.1");
        long res = dataService.calculateUV(new Date(), new Date());
        System.out.println(res);
    }

    @Test
    public void test2() {
        dataService.recordDAU(1);
        dataService.recordDAU(1);
        dataService.recordDAU(1);
        dataService.recordDAU(2);

        dataService.recordDAU(3);
        long res = dataService.calculateDAU(new Date(), new Date());
        System.out.println(res);
    }
}

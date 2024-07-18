package com.summer.community;

import com.summer.community.entity.Message;
import com.summer.community.service.MessageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-07-14:20
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MessageTests {

    @Autowired
    private MessageService messageService;

    @Test
    public void selectConversationsTest() {
        List<Message> list = messageService.findConversations(2, 0, 10);
        System.out.println(list);
    }

    @Test
    public void selectConversationCountTest() {
        int count = messageService.findConversationCount(5);
        System.out.println(count);
    }

    @Test
    public void selectLetterCountTest() {
        int count = messageService.findLetterCount("2_3");
        System.out.println(count);
    }

    @Test
    public void systemLetterTest() {
        List<Message> list = messageService.findConversations(5, 0, 10);
        System.out.println(list);
    }

    @Test
    public void selectLikeNoticeTest() {
        Message message = messageService.findLatestNotice(3, "like");
        System.out.println(message);
    }
}

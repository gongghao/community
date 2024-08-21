package com.summer.community.controller;

import com.alibaba.druid.sql.visitor.functions.Nil;
import com.alibaba.fastjson.JSONObject;
import com.summer.community.entity.Message;
import com.summer.community.entity.Page;
import com.summer.community.entity.Result;
import com.summer.community.entity.User;
import com.summer.community.service.MessageService;
import com.summer.community.service.UserService;
import com.summer.community.util.CommunityConstant;
import com.summer.community.util.CommunityUtil;
import com.summer.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.swing.text.MaskFormatter;
import java.util.*;

/**
 * @Version: java version 20
 * @Author: Hao G
 * @date: 2024-07-07-16:21
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList() {
        Result result = Result.ok("/letter/list");
        Page page = new Page();

        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList =
                messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null)
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = (user.getId() == message.getFromId()) ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        //model.addAttribute("conversations", conversations);
        result.data("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        //model.addAttribute("letterUnreadCount", letterUnreadCount);
        result.data("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        //model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        result.data("noticeUnreadCount", noticeUnreadCount);

        //return "/site/letter";
        result.data("target", "/site/letter");
        result.data("Page", page);
        return result.toString();
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    @ResponseBody
    public String getLetterDetail(@PathVariable("conversationId") String conversationId) {
        Result result = Result.ok("/letter/detail/{conversationId}.get");
        Page page = new Page();

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null)
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        //model.addAttribute("letters", letters);
        result.data("letters", letters);

        // 私信目标
        //model.addAttribute("target", getLetterTarget(conversationId));
        result.data("targetUser", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty())
            messageService.readMessage(ids);

        result.data("Page", page);
        result.data("target", "/site/letter-detail");
        //return "/site/letter-detail";
        return result.toString();
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0)
            return userService.findUserById(id1);
        else return userService.findUserById(id0);
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        Result result = Result.ok("/letter/send.post");

        User target = userService.findUserByName(toName);
        if (target == null)
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId())
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        else
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        //return CommunityUtil.getJSONString(0);
        return result.toString();
    }

    private List<Integer> getLetterIds(List<Message> letters) {
        List<Integer> ids = new ArrayList<>();
        if (letters != null)
            for (Message message : letters)
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0)
                    ids.add(message.getId());
        return ids;
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    @ResponseBody
    public String getNoticeList() {
        Result result = Result.ok("/notice/list.get");

        User user = hostHolder.getUser();

        // 查询评论类的通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("unread", unread);
        } else
            messageVo.put("message", null);
        //model.addAttribute("commentNotice", messageVo);
        result.data("commentNotice",messageVo);

        // 查询点赞类的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVo.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVo.put("unread", unread);
        } else
            messageVo.put("message", null);
        //model.addAttribute("likeNotice", messageVo);
        result.data("likeNotice", messageVo);

        // 查询关注类的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if (message != null) {
            messageVo.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("unread", unread);
        } else
;            messageVo.put("message", null);
        //model.addAttribute("followNotice", messageVo);
        result.data("followNotice", messageVo);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        //model.addAttribute("letterUnreadCount", letterUnreadCount);
        result.data("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        //model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        result.data("noticeUnreadCount", noticeUnreadCount);

        result.data("target", "/site/notice");
        return result.toString();
        //return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    @ResponseBody
    public String getNoticeDetail(@PathVariable("topic") String topic) {
        Result result = Result.ok("/notice/detail/{topic}.get");
        Page page = new Page();

        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeVoList != null)
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();

                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        //model.addAttribute("notices", noticeVoList);
        result.data("notices", noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty())
            messageService.readMessage(ids);

        //return "/site/notice-detail";
        result.data("target", "/site/notice-detail");
        return result.toString();
    }

}

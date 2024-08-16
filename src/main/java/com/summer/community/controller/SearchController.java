package com.summer.community.controller;

import com.summer.community.entity.DiscussPost;
import com.summer.community.entity.Page;
import com.summer.community.entity.Result;
import com.summer.community.service.ElasticsearchService;
import com.summer.community.service.LikeService;
import com.summer.community.service.UserService;
import com.summer.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Version: java version 20
 * @Author: Wei Zhou
 * @date: 2024-07-25-10:51
 */
@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    @ResponseBody
    public String search(String keyword, Model model) {
        Result result = Result.ok("/search");
        Page page = new Page();

        //搜索帖子
        List<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
//        System.out.println(searchResult.size());
//        System.out.println(searchResult.size());
//        System.out.println(searchResult.size());
//        System.out.println(searchResult.size());
//        System.out.println(searchResult.size());
//        System.out.println(page.getOffset());
//        System.out.println(page.getLimit());
//        System.out.println(page.getCurrent());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                map.put("post", post);
                //作者
                map.put("user", userService.findUserById(post.getUserId()));
                //点赞
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }

        //model.addAttribute("discussPosts", discussPosts);
        //model.addAttribute("keyword", keyword);
        result.data("discussPosts", discussPosts);
        result.data("keyword", keyword);

        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : searchResult.size());

        result.data("page", page);
        return "/site/search";
    }
}

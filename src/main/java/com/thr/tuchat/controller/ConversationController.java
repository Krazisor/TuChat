package com.thr.tuchat.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.pojo.Conversation;
import com.thr.tuchat.pojo.ResponseResult;
import com.thr.tuchat.service.ConversationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Resource
    private ConversationService conversationService;

    @GetMapping("/list")
    public ResponseResult<List<Conversation>> getConversationByUserId () {
        try {
            String userId = StpUtil.getLoginIdAsString();
            log.info("用户正在请求Conversation列表，用户ID#{}", userId);
            List<Conversation> conversationList = conversationService.getConversationByUserId_Safe(userId);
            return ResponseResult.success(conversationList);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }
}

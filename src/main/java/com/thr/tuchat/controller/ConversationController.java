package com.thr.tuchat.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.pojo.Conversation;
import com.thr.tuchat.pojo.ResponseResult;
import com.thr.tuchat.service.ConversationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Resource
    private ConversationService conversationService;

    @SaCheckLogin
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

    @SaCheckLogin
    @GetMapping("/add")
    public ResponseResult<String> addNewConversationByName (@RequestParam String title) {
        try {
            log.info("用户请求新增会话,#{}", title);
            String conversationId = conversationService.addNewConversation(title);
            return ResponseResult.success(conversationId);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }
}

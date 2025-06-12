package com.thr.tuchat.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.ServiceDeniedException;
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
    public ResponseResult<List<Conversation>> getConversationByUserId() {
        String userId = StpUtil.getLoginIdAsString();
        log.info("用户正在请求Conversation列表，用户ID#{}", userId);
        List<Conversation> conversationList = conversationService.getConversationByUserId_Safe(userId);
        return ResponseResult.success(conversationList);
    }

    @SaCheckLogin
    @GetMapping("/add")
    public ResponseResult<String> addNewConversationByName(@RequestParam String title) {
        log.info("用户请求新增会话,#{}", title);
        String conversationId = conversationService.addNewConversation(title);
        return ResponseResult.success(conversationId);
    }

    @SaCheckLogin
    @GetMapping("/delete")
    public ResponseResult<Boolean> deleteConversationWithMessage(@RequestParam String conversationId) {
        log.info("用户删除会话以及对话信息,#{}", conversationId);
        if (conversationService.deleteConversationWithMessage(conversationId)) {
            return ResponseResult.success(true);
        } else {
            throw new ServiceDeniedException("无法删除对话以及对话信息");
        }

    }

    @SaCheckLogin
    @PostMapping("/update")
    public ResponseResult<Boolean> updateConversationById(Conversation conversation) {
        log.info("用户尝试更新会话信息, {}", conversation);
        if (conversationService.updateConversationById(conversation)) {
            return ResponseResult.success(true);
        } else {
            throw new ServiceDeniedException("无法更改会话信息");
        }
    }
}

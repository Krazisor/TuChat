package com.thr.tuchat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.model.entity.Message;
import com.thr.tuchat.common.ResponseResult;
import com.thr.tuchat.service.MessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @SaCheckLogin
    @GetMapping("/list")
    public ResponseResult<List<Message>> getListMessageByConversationId(@RequestParam String conversationId) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户正在请求对话记录，会话ID:#{}", conversationId);
        List<Message> messageList = messageService.getAllMessageByConversationId(conversationId);
        return ResponseResult.success(messageList);
    }
}

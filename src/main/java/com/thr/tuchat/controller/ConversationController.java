package com.thr.tuchat.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.dto.ConversationRenameRequest;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.pojo.Conversation;
import com.thr.tuchat.common.ResponseResult;
import com.thr.tuchat.service.ConversationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户正在请求Conversation列表，用户ID#{}", userId);
        List<Conversation> conversationList = conversationService.getConversationByUserId(userId);
        return ResponseResult.success(conversationList);
    }

    @SaCheckLogin
    @GetMapping("/add")
    public ResponseResult<String> addNewConversation(@RequestParam String title) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(title == null, ResultCode.PARAMS_ERROR, "未提供title");
        log.info("用户请求新增会话,#{}", title);
        String conversationId = conversationService.addNewConversation(title);
        return ResponseResult.success(conversationId);
    }

    @SaCheckLogin
    @PostMapping("/rename")
    public ResponseResult<Boolean> renameConversation(@RequestBody ConversationRenameRequest conversationRenameRequest) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(Objects.isNull(conversationRenameRequest), ResultCode.PARAMS_ERROR,
                "conversationRenameRequest不存在");
        Boolean success = conversationService.renameConversation(conversationRenameRequest.getConversationId(),
                conversationRenameRequest.getNewTitle());
        return ResponseResult.success(success);
    }

}

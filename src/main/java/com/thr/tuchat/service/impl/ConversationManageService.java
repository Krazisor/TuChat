package com.thr.tuchat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.service.ConversationService;
import com.thr.tuchat.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ConversationManageService {
    @Resource
    private ConversationService conversationService;
    @Resource
    private MessageService messageService;

    @Transactional
    public boolean deleteConversationWithMessage(String conversationId) {
        String userId = StpUtil.getLoginIdAsString();
        String conversationOwnerId = conversationService.getUserIdByConversationId(conversationId);
        if (!Objects.equals(userId, conversationOwnerId)) {
            throw new BusinessException(ResultCode.NO_AUTH_ERROR, "用户无权删除该会话");
        }
        messageService.deleteMessagesByConversationId(conversationId);
        conversationService.deleteConversationById(conversationId);
        return true;
    }
}

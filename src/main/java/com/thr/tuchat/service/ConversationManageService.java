package com.thr.tuchat.service;

import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.ServiceDeniedException;
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
            throw new ServiceDeniedException("用户无权删除该会话");
        }
        messageService.deleteMessagesByConversationId(conversationId);
        conversationService.deleteConversationById(conversationId);
        return true;
    }
}

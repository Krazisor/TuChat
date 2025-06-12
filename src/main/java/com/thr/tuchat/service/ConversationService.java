package com.thr.tuchat.service;

import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.ServiceDeniedException;
import com.thr.tuchat.mapper.ConversationMapper;
import com.thr.tuchat.pojo.Conversation;
import com.thr.tuchat.pojo.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ConversationService {

    @Resource
    private ConversationMapper conversationMapper;

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    /**
     * @param userId 用户ID
     * @return List<Conversation>
     */
    public List<Conversation> getConversationByUserId_Safe(String userId) {
        String loginIdAsString = StpUtil.getLoginIdAsString();
        if (!Objects.equals(loginIdAsString, userId)) {
            throw new ServiceDeniedException("用户提供了不属于自己的userId");
        }
        return conversationMapper.getConversationByUserId(userId);
    }

    public Conversation getConversationById(String conversationId) {
        String userId = StpUtil.getLoginIdAsString();
        Conversation conversation = conversationMapper.getConversationById(conversationId);
        if (conversation == null) {
            throw new ServiceDeniedException("会话不存在");
        }
        if (!Objects.equals(userId, conversation.getUserId())) {
            throw new ServiceDeniedException("用户索取了不属于自己的getConversationById");
        }
        return conversation;
    }

    public String getUserIdByConversationId(String conversationId) {
        String userId = conversationMapper.getUserIdByConversationId(conversationId);
        if (userId == null) {
            throw new ServiceDeniedException("未找到指定会话的用户id");
        }
        return userId;
    }

    public String addNewConversation(String title) {
        String userId = StpUtil.getLoginIdAsString();
        String conversationId = UUID.randomUUID().toString();
        Conversation conversation = new Conversation(conversationId, userId, title, null, false);
        conversationMapper.addNewConversation(conversation);
        return conversationId;
    }

    @Transactional
    public boolean deleteConversationWithMessage(String conversationId) {
        String userId = StpUtil.getLoginIdAsString();
        String conversationOwnerId = getUserIdByConversationId(conversationId);
        if (!Objects.equals(userId, conversationOwnerId)) {
            throw new ServiceDeniedException("用户无权删除该会话");
        }
        messageService.deleteMessagesByConversationId(conversationId);
        conversationMapper.deleteConversationById(conversationId);
        return true;
    }

    public boolean updateConversationById(Conversation conversation) {
        String userId = StpUtil.getLoginIdAsString();
        String ownerId = getUserIdByConversationId(conversation.getConversationId());
        if (!Objects.equals(userId, ownerId)) {
            throw new ServiceDeniedException("用户无权限修改该会话");
        }
        if (!Objects.equals(conversation.getUserId(), userId)) {
            User user = userService.getUserById(conversation.getUserId());
            if (user == null) {
                throw new ServiceDeniedException("updateConversationById时找不到要易主的userId");
            }
        }
        conversationMapper.updateConversationById(conversation);
        return true;
    }
}
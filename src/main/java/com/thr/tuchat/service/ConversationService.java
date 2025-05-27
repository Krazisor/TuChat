package com.thr.tuchat.service;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.mapper.ConversationMapper;
import com.thr.tuchat.pojo.Conversation;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ConversationService {

    @Resource
    private ConversationMapper conversationMapper;


    /**
     * @param userId 用户ID
     * @return List<Conversation>
     */
    public List<Conversation> getConversationByUserId_Safe(String userId) {
        try {
            String loginIdAsString = StpUtil.getLoginIdAsString();
            if (Objects.equals(loginIdAsString, userId)) {
                return conversationMapper.getConversationByUserId(userId);
            } else {
                throw new RuntimeException("用户提供了不属于自己的userId");
            }
        } catch (Exception e) {
            throw new RuntimeException("getConversationByUserId出现了意料之外的错误" + e.getMessage());
        }
    }

    public Conversation getConversationById(String conversationId) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            Conversation conversation = conversationMapper.getConversationById(conversationId);
            if (Objects.equals(userId, conversation.getUserId())) {
                return conversation;
            } else {
                throw new RuntimeException("用户索取了不属于自己的getConversationById");
            }
        } catch (Exception e) {
            throw new RuntimeException("getConversationById出现了意料之外的错误" + e.getMessage());
        }
    }

    public String getUserIdByConversationId (String conversationId) {
        try {
            return conversationMapper.getUserIdByConversationId(conversationId);
        } catch (Exception e) {
            throw new RuntimeException("getUserIdByConversationId出现了意料之外的错误" + e.getMessage());
        }
    }

    public String addNewConversation (String title) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            String conversationId = UUID.randomUUID().toString();
            Conversation conversation = new Conversation(conversationId, userId, title, null, false);
            conversationMapper.addNewConversation(conversation);
            return conversationId;
        } catch (Exception e) {
            throw new RuntimeException("addNewConversation出现了意料之外的错误" + e.getMessage());
        }
    }
}

package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.model.entity.Conversation;

import java.util.List;

public interface ConversationService extends IService<Conversation> {
    List<Conversation> getConversationByUserId(String userId);

    Conversation getConversationById(String conversationId);

    String getUserIdByConversationId(String conversationId);

    String addNewConversation(String title);

    Boolean renameConversation(String conversationId, String newTitle);

    void deleteConversationById(String conversationId);
}

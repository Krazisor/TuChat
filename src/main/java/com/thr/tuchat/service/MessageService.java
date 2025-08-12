package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.model.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {
    List<Message> getAllMessageByConversationId(String conversationId);

    void addNewMessage(Message message);

    void deleteMessagesByConversationId(String conversationId);
}

package com.thr.tuchat.service;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.MessageMapper;
import com.thr.tuchat.pojo.Conversation;
import com.thr.tuchat.pojo.Message;
import com.thr.tuchat.pojo.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private ConversationService conversationService;

    public List<Message> getAllMessageByConversationId(String conversationId) {
        String userId = StpUtil.getLoginIdAsString();
        Conversation conversation = conversationService.getConversationById(conversationId);
        ThrowUtils.throwIf(!Objects.equals(userId, conversation.getUserId()), ResultCode.NO_AUTH_ERROR,
                "用户想要获取不属于自己的message");
        return messageMapper.getAllMessageByConversationId(conversationId);
    }

    public void addNewMessage(Message message) {
        ThrowUtils.throwIf(Objects.isNull(message), ResultCode.PARAMS_ERROR, "未提供message");
        messageMapper.addNewMessage(message);
    }
}

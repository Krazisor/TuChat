package com.thr.tuchat.service;


import cn.dev33.satoken.stp.StpUtil;
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

    public List<Message> getAllMessageByConversationId (String conversationId) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            Conversation conversation = conversationService.getConversationById(conversationId);
            if (Objects.equals(userId, conversation.getUserId())) {
                return messageMapper.getAllMessageByConversationId(conversationId);
            } else {
                throw new RuntimeException("用户想要获取不属于自己的message");
            }
        } catch (Exception e) {
            throw new RuntimeException("getAllMessageByConversationId发生异常:"+ e.getMessage());
        }
    }

    public void addNewMessage (Message message) {
        try {
            log.info("message存入数据库：{}", message);
            messageMapper.addNewMessage(message);
        } catch (Exception e) {
            throw new RuntimeException("addNewMessage发生异常:"+ e.getMessage());
        }
    }
}

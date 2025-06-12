package com.thr.tuchat.service;

import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.ServiceDeniedException; // 需导入你的自定义异常
import com.thr.tuchat.mapper.MessageMapper;
import com.thr.tuchat.pojo.Conversation;
import com.thr.tuchat.pojo.Message;
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
        if (!Objects.equals(userId, conversation.getUserId())) {
            throw new ServiceDeniedException("用户想要获取不属于自己的message");
        }
        return messageMapper.getAllMessageByConversationId(conversationId);
    }

    public void addNewMessage(Message message) {
        log.info("message存入数据库：{}", message);
        messageMapper.addNewMessage(message);
    }

    public void deleteMessagesByConversationId(String conversationId) {
        log.info("删除conversationId: {}下的所有message", conversationId);
        messageMapper.deleteMessagesByConversationId(conversationId);
    }
}
package com.thr.tuchat.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.MessageMapper;
import com.thr.tuchat.model.entity.Conversation;
import com.thr.tuchat.model.entity.Message;
import com.thr.tuchat.service.MessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private ConversationServiceImpl conversationService;

    @Override
    public List<Message> getAllMessageByConversationId(String conversationId) {
        String userId = StpUtil.getLoginIdAsString();
        Conversation conversation = conversationService.getConversationById(conversationId);
        ThrowUtils.throwIf(!Objects.equals(userId, conversation.getUserId()), ResultCode.NO_AUTH_ERROR,
                "用户想要获取不属于自己的message");
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<Message>();
        queryWrapper.eq(Message::getConversationId, conversationId);
        return messageMapper.selectList(queryWrapper);
    }

    @Override
    public void addNewMessage(Message message) {
        ThrowUtils.throwIf(Objects.isNull(message), ResultCode.PARAMS_ERROR, "未提供message");
        messageMapper.insert(message);
    }

    @Override
    public void deleteMessagesByConversationId(String conversationId) {
        log.info("删除conversationId:{}下的所有message", conversationId);
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId);
        messageMapper.delete(queryWrapper);
    }
}

package com.thr.tuchat.service;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
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
    public List<Conversation> getConversationByUserId(String userId) {
        ThrowUtils.throwIf(userId == null, ResultCode.PARAMS_ERROR, "未提供userId");
        return conversationMapper.getConversationByUserId(userId);
    }

    public Conversation getConversationById(String conversationId) {
        ThrowUtils.throwIf(conversationId == null, ResultCode.PARAMS_ERROR, "未提供conversationId");
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "未获取到当前登录ID");
        Conversation conversation = conversationMapper.getConversationById(conversationId);
        ThrowUtils.throwIf(conversation == null, ResultCode.NOT_FOUND_ERROR, "不存在的conversation");
        ThrowUtils.throwIf(!Objects.equals(userId, conversation.getUserId()), ResultCode.NO_AUTH_ERROR,
                "用户获取了不属于自己的conversation");
        return conversation;
    }

    public String getUserIdByConversationId(String conversationId) {
        ThrowUtils.throwIf(conversationId == null, ResultCode.PARAMS_ERROR, "未提供conversationId");
        String userId = conversationMapper.getUserIdByConversationId(conversationId);
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_FOUND_ERROR, "无法找到对应的userId");
        return userId;
    }

    public String addNewConversation(String title) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        String conversationId = UUID.randomUUID().toString();
        ThrowUtils.throwIf(conversationId == null, ResultCode.OPERATION_ERROR,
                "生成UUID失败");
        Conversation conversation = new Conversation(conversationId, userId, title, null, false);
        conversationMapper.addNewConversation(conversation);
        return conversationId;
    }
}

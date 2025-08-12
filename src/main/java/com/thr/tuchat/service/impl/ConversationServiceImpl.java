package com.thr.tuchat.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.ConversationMapper;
import com.thr.tuchat.model.entity.Conversation;
import com.thr.tuchat.service.ConversationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Resource
    private ConversationMapper conversationMapper;


    /**
     * @param userId 用户ID
     * @return List<Conversation>
     */
    @Override
    public List<Conversation> getConversationByUserId(String userId) {
        ThrowUtils.throwIf(userId == null, ResultCode.PARAMS_ERROR, "未提供userId");
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getCreateTime);
        return conversationMapper.selectList(queryWrapper);
    }

    @Override
    public Conversation getConversationById(String conversationId) {
        ThrowUtils.throwIf(conversationId == null, ResultCode.PARAMS_ERROR, "未提供conversationId");
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "未获取到当前登录ID");
        Conversation conversation = conversationMapper.selectById(conversationId);
        ThrowUtils.throwIf(conversation == null, ResultCode.NOT_FOUND_ERROR, "不存在的conversation");
        ThrowUtils.throwIf(!Objects.equals(userId, conversation.getUserId()), ResultCode.NO_AUTH_ERROR,
                "用户获取了不属于自己的conversation");
        return conversation;
    }

    @Override
    public String getUserIdByConversationId(String conversationId) {
        ThrowUtils.throwIf(conversationId == null, ResultCode.PARAMS_ERROR, "未提供conversationId");
        String userId = conversationMapper.selectById(conversationId).getUserId();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_FOUND_ERROR, "无法找到对应的userId");
        return userId;
    }

    @Override
    public String addNewConversation(String title) {
        String userId = StpUtil.getLoginIdAsString();
        Conversation conversation = new Conversation();
        conversation.setUserId(userId).setTitle(title).setIsMarked(false);
        conversationMapper.insert(conversation);
        return conversation.getConversationId();
    }

    @Override
    public Boolean renameConversation(String conversationId, String newTitle) {
        log.info("正在修改对话:{}, 将其重命名为: {}", conversationId, newTitle);
        ThrowUtils.throwIf(conversationId == null || newTitle == null, ResultCode.PARAMS_ERROR,
                "不存在的rename");
        Conversation conversation = this.getConversationById(conversationId);
        ThrowUtils.throwIf(Objects.isNull(conversation), ResultCode.NOT_FOUND_ERROR, "不存在的conversation");
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(!Objects.equals(conversation.getUserId(), userId), ResultCode.NO_AUTH_ERROR,
                "正在重命名不属于自己的conversation");
        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Conversation::getTitle, newTitle).eq(Conversation::getUserId, userId);
        conversationMapper.update(null, updateWrapper);
        return true;
    }

    @Override
    public void deleteConversationById(String conversationId) {
        ThrowUtils.throwIf(conversationId == null, ResultCode.PARAMS_ERROR, "conversationId为空");
        String userId = StpUtil.getLoginIdAsString();
        Conversation conversation = this.getConversationById(conversationId);
        ThrowUtils.throwIf(Objects.isNull(conversation), ResultCode.NOT_FOUND_ERROR, "不存在的conversation");
        ThrowUtils.throwIf(!Objects.equals(userId, conversation.getUserId()), ResultCode.NO_AUTH_ERROR,
                "正在删除不属于自己的conversationId");
        conversationMapper.deleteById(conversationId);
    }
}

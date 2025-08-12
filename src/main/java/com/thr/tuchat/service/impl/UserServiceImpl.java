package com.thr.tuchat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.UserMapper;
import com.thr.tuchat.model.entity.User;
import com.thr.tuchat.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MinioService minioService;

    @Transactional
    @Override
    public void saveOrUpdateUser(User user) {
        User existingUser = userMapper.selectById(user.getUserId());
        if (existingUser != null) {
            // 如果用户存在，执行更新 更新头像
            log.info("当前用户已存在: #{}", existingUser);
            user.setAvatar(existingUser.getAvatar());
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<User>()
                    .eq(User::getUserId, user.getUserId());
            userMapper.update(user, updateWrapper);
        } else {
            // 如果用户不存在，执行插入
            log.info("当前用户已存在，插入用户: #{}", user);
            userMapper.insert(user);
        }
    }

    @Override
    public User getUserById(String userId) {
        User user = userMapper.selectById(userId);
        try {
            String tempURL = minioService.getTemporaryURL(user.getAvatar());
            user.setAvatar(tempURL);
            return user;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "无法获取临时URL");
        }
    }

    @Transactional
    @Override
    public String updateUserAvatar(MultipartFile file) {
        try {
            String URL = minioService.upload(file);
            ThrowUtils.throwIf(URL == null, ResultCode.OPERATION_ERROR, "无法更新用户头像");
            String userId = StpUtil.getLoginIdAsString();
            User user = getUserById(userId);
            ThrowUtils.throwIf(Objects.isNull(user), ResultCode.NOT_FOUND_ERROR, "不存在的用户");
            user.setAvatar(URL);
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<User>()
                    .set(User::getAvatar, user.getAvatar())
                    .eq(User::getUserId, user.getUserId());
            userMapper.update(null, updateWrapper);
            return URL;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "无法上传用户头像");
        }
    }

}

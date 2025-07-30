package com.thr.tuchat.service;

import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.UserMapper;
import com.thr.tuchat.pojo.User;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MinioService minioService;

    @Transactional
    public void saveOrUpdateUser(User user) {
        User existingUser = userMapper.getUserById(user.getUserId());
        if (existingUser != null) {
            // 如果用户存在，执行更新 更新头像
            log.info("当前用户已存在: #{}", existingUser);
            user.setAvatar(existingUser.getAvatar());
            userMapper.updateUser(user);
        } else {
            // 如果用户不存在，执行插入
            log.info("当前用户已存在，插入用户: #{}", user);
            userMapper.addUser(user);
        }
    }

    public User getUserById(String userId) {
        User user = userMapper.getUserById(userId);
        try {
            String tempURL = minioService.getTemporaryURL(user.getAvatar());
            user.setAvatar(tempURL);
            return user;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "无法获取临时URL");
        }
    }

    @Transactional
    public String updateUserAvatar(MultipartFile file) {
        try {
            String URL = minioService.upload(file);
            ThrowUtils.throwIf(URL == null, ResultCode.OPERATION_ERROR, "无法更新用户头像");
            String userId = StpUtil.getLoginIdAsString();
            User user = getUserById(userId);
            ThrowUtils.throwIf(Objects.isNull(user), ResultCode.NOT_FOUND_ERROR, "不存在的用户");
            user.setAvatar(URL);
            userMapper.updateUser(user);
            return URL;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "无法上传用户头像");
        }
    }

}

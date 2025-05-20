package com.thr.tuchat.service;

import cn.dev33.satoken.stp.StpUtil;
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

    public User getUserById(String userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, URISyntaxException {
        User user = userMapper.getUserById(userId);
        String tempURL = minioService.getTemporaryURL(user.getAvatar());
        user.setAvatar(tempURL);
        return user;
    }

    @Transactional
    public String updateUserAvatar(MultipartFile file) {
        try {
            String URL = minioService.upload(file);
            if (URL != null) {
                String userId = StpUtil.getLoginIdAsString();
                User user = getUserById(userId);
                user.setAvatar(URL);
                userMapper.updateUser(user);
                return URL;
            } else {
                throw new RuntimeException("文件上传失败");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

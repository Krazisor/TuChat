package com.thr.tuchat.service;

import com.thr.tuchat.mapper.UserMapper;
import com.thr.tuchat.pojo.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

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
        return userMapper.getUserById(userId);
    }
}

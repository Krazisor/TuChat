package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.model.entity.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {
    @Transactional
    void saveOrUpdateUser(User user);

    User getUserById(String userId);

    @Transactional
    String updateUserAvatar(MultipartFile file);
}

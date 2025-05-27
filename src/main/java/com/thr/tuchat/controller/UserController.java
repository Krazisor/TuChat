package com.thr.tuchat.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.pojo.ResponseResult;
import com.thr.tuchat.pojo.User;
import com.thr.tuchat.service.UserService;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/getInfo")
    public ResponseResult<User> getUserInfo () {
        try {
            String userId = StpUtil.getLoginIdAsString();
            log.info("用户正在获取个人信息，#{}",userId);
            User user = userService.getUserById(userId);
            return ResponseResult.success(user);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }

    @PostMapping("/updateAvatar")
    public ResponseResult<String> updateUserAvatar (@RequestParam("file") MultipartFile file) {
        try {
            String URL = userService.updateUserAvatar(file);
            log.info("用户正在更新头像，#{}",URL);
            return ResponseResult.success(URL);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }
}

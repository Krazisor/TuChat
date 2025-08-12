package com.thr.tuchat.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.common.ResponseResult;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.model.entity.User;
import com.thr.tuchat.service.impl.UserServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserServiceImpl userService;

    @SaCheckLogin
    @GetMapping("/getInfo")
    public ResponseResult<User> getUserInfo() {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户正在获取个人信息，#{}", userId);
        User user = userService.getUserById(userId);
        return ResponseResult.success(user);
    }

    @SaCheckLogin
    @PostMapping("/updateAvatar")
    public ResponseResult<String> updateUserAvatar(@RequestParam("file") MultipartFile file) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        String URL = userService.updateUserAvatar(file);
        log.info("用户正在更新头像:{}", URL);
        return ResponseResult.success(URL);
    }
}

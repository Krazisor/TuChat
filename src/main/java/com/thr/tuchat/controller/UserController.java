package com.thr.tuchat.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.pojo.ResponseResult;
import com.thr.tuchat.pojo.User;
import com.thr.tuchat.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/getInfo")
    public ResponseResult<User> getUserInfo () {
        String userId = StpUtil.getLoginIdAsString();
        User user = userService.getUserById(userId);
        return ResponseResult.success(user);
    }
}

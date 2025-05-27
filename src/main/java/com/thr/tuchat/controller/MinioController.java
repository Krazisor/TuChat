package com.thr.tuchat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.thr.tuchat.pojo.ResponseResult;
import com.thr.tuchat.service.MinioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@CrossOrigin
public class MinioController {

    @Resource
    private MinioService minioService;

    @SaCheckLogin
    @PostMapping("/upload")
    public ResponseResult<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("用户正在上传文件#{}",file);
            String URL = minioService.upload(file);
            return ResponseResult.success(URL);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }
}
package com.thr.tuchat.controller;

import cn.dev33.satoken.util.SaResult;
import com.thr.tuchat.pojo.ResponseResult;
import com.thr.tuchat.service.MinioService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
public class MinioController {

    @Resource
    private MinioService minioService;

    @PostMapping("/upload")
    public ResponseResult<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String URL = minioService.upload(file);
            return ResponseResult.success(URL);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }
}
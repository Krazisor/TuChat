package com.thr.tuchat.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.common.ResponseResult;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.model.dto.FileListResponse;
import com.thr.tuchat.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @SaCheckLogin
    @PostMapping("/uploadFile")
    public ResponseResult<List<String>> uploadFileByBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("knowledgeBaseId") String knowledgeBaseId) {

        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(files.length == 0, ResultCode.PARAMS_ERROR, "上传的文件列表为空");
        ThrowUtils.throwIf(Objects.isNull(knowledgeBaseId), ResultCode.PARAMS_ERROR, "知识库的为空");

        // 并行上传
        // 这用的是默认的ForkJoinPool线程池，可以显著提升I/O型任务“批量上传”的吞吐率。
        List<CompletableFuture<String>> futureList = new ArrayList<>();
        String tokenValue = StpUtil.getTokenValue();
        for (MultipartFile file : files) {
            // 关键：为了使用异步线程池，我们需要将Satoken的上下文手动设置到子线程中，即Mock 出一个 Web 上下文填充到上下文管理器中
            // 使用satoken自带的mock，可以防止线程池线程复用导致的上下文污染
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                    SaTokenContextMockUtil.setMockContext(() -> {
                                StpUtil.setTokenValueToStorage(tokenValue);
                                return fileService.uploadFileToKnowledgeBase(file, knowledgeBaseId);
                            }
                    ));
            futureList.add(future);
        }
        List<String> fileIdList = futureList.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return ResponseResult.success(fileIdList);
    }

    @SaCheckLogin
    @GetMapping("/list")
    public ResponseResult<List<FileListResponse>> list(@RequestParam String knowledgeBaseId) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(knowledgeBaseId.isEmpty(), ResultCode.PARAMS_ERROR, "请求的知识库Id为空");
        log.info("用户正在获取文件列表，知识库为：{}", knowledgeBaseId);
        List<FileListResponse> fileListResponseList = fileService.getFileListByKnowledgeBaseId(knowledgeBaseId);
        return ResponseResult.success(fileListResponseList);
    }
}

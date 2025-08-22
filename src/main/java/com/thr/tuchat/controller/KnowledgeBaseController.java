package com.thr.tuchat.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.common.ResponseResult;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.model.dto.KnowledgeBaseListResponse;
import com.thr.tuchat.model.dto.NewKnowledgeBaseRequest;
import com.thr.tuchat.service.KnowledgeBaseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/knowledgeBase")
public class KnowledgeBaseController {

    @Resource
    private KnowledgeBaseService knowledgeBaseService;


    @GetMapping("/list")
    public ResponseResult<List<KnowledgeBaseListResponse>> list(){
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户:{} 正在请求知识库列表", userId);
        List<KnowledgeBaseListResponse> knowledgeBaseList = knowledgeBaseService.getKnowledgeBaseList(userId);
        return ResponseResult.success(knowledgeBaseList);
    }

    @PostMapping("/add")
    public ResponseResult<String> add(@RequestBody NewKnowledgeBaseRequest newKnowledgeBaseRequest){
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户:{} 正在请求新建知识库: {}", userId,  newKnowledgeBaseRequest);
        String knowledgeBaseId = knowledgeBaseService.addNewKnowledgeBase(
                newKnowledgeBaseRequest.name(),
                newKnowledgeBaseRequest.description(),
                userId);
        return ResponseResult.success(knowledgeBaseId);
    }


}

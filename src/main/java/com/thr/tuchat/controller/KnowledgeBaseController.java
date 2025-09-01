package com.thr.tuchat.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.thr.tuchat.common.ResponseResult;
import com.thr.tuchat.constant.RoleEnum;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.model.dto.KnowledgeBaseListResponse;
import com.thr.tuchat.model.dto.KnowledgeBaseReOwnerRequest;
import com.thr.tuchat.model.dto.KnowledgeUpdateRequest;
import com.thr.tuchat.model.dto.NewKnowledgeBaseRequest;
import com.thr.tuchat.service.KnowledgeBaseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/knowledgeBase")
public class KnowledgeBaseController {

    @Resource
    private KnowledgeBaseService knowledgeBaseService;


    @GetMapping("/list")
    public ResponseResult<List<KnowledgeBaseListResponse>> list() {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户:{} 正在请求知识库列表", userId);
        List<KnowledgeBaseListResponse> knowledgeBaseList = knowledgeBaseService.getKnowledgeBaseList(userId);
        return ResponseResult.success(knowledgeBaseList);
    }

    @GetMapping("/member")
    public ResponseResult<Map<RoleEnum, List<String>>> getKnowledgeBaseMember(@RequestParam String knowledgeBaseId) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户:{} 正在请求知识库成员", userId);
        Map<RoleEnum, List<String>> ans = knowledgeBaseService.getKnowledgeBaseMember(knowledgeBaseId);
        return ResponseResult.success(ans);
    }

    @PostMapping("/add")
    public ResponseResult<String> addKnowledgeBase(@RequestBody NewKnowledgeBaseRequest newKnowledgeBaseRequest) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户:{} 正在请求新建知识库: {}", userId, newKnowledgeBaseRequest);
        String knowledgeBaseId = knowledgeBaseService.addNewKnowledgeBase(
                newKnowledgeBaseRequest.name(),
                newKnowledgeBaseRequest.description(),
                userId);
        return ResponseResult.success(knowledgeBaseId);
    }

    @GetMapping("/delete")
    public ResponseResult<Boolean> deleteKnowledgeBase(@RequestParam String knowledgeBaseId) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户:{}, 正在删除知识库: {}", userId, knowledgeBaseId);
        Boolean ans = knowledgeBaseService.deleteKnowledgeBaseWithFile(knowledgeBaseId);
        return ResponseResult.success(ans);
    }

    @PostMapping("/update")
    public ResponseResult<Boolean> updateKnowledgeBase(@RequestBody KnowledgeUpdateRequest knowledgeUpdateRequest) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户：{}，正在更新知识库信息：{}", userId, knowledgeUpdateRequest);
        ThrowUtils.throwIf(knowledgeUpdateRequest.knowledgeBaseId() == null, ResultCode.PARAMS_ERROR,
                "未提交知识库Id");
        Boolean success = knowledgeBaseService.updateKnowledgeBaseInfo(
                knowledgeUpdateRequest.knowledgeBaseId(),
                knowledgeUpdateRequest.name(),
                knowledgeUpdateRequest.description(),
                knowledgeUpdateRequest.editorList(),
                knowledgeUpdateRequest.viewerList());
        return ResponseResult.success(success);
    }

    @PostMapping("/reOwner")
    public ResponseResult<Boolean> reOwnerKnowledgeBase(@RequestBody KnowledgeBaseReOwnerRequest knowledgeBaseReOwnerRequest) {
        String userId = StpUtil.getLoginIdAsString();
        ThrowUtils.throwIf(userId == null, ResultCode.NOT_LOGIN_ERROR, "用户未登录");
        log.info("用户：{}，正在易主知识库信息：{}", userId, knowledgeBaseReOwnerRequest);
        Boolean success = knowledgeBaseService.changeKnowledgeBaseOwner(
                knowledgeBaseReOwnerRequest.knowledgeBaseId(),
                knowledgeBaseReOwnerRequest.newOwnerId(),
                knowledgeBaseReOwnerRequest.newRole()
        );
        return ResponseResult.success(success);
    }
}

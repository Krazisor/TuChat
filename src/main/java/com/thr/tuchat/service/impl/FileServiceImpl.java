package com.thr.tuchat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.exception.ThrowUtils;
import com.thr.tuchat.mapper.FileMapper;
import com.thr.tuchat.mapper.KnowledgeBaseMapper;
import com.thr.tuchat.model.dto.FileListResponse;
import com.thr.tuchat.model.entity.File;
import com.thr.tuchat.model.entity.KnowledgeBase;
import com.thr.tuchat.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Resource
    private FileMapper fileMapper;

    @Resource
    private MinioService minioService;

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public List<FileListResponse> getFileListByKnowledgeBaseId(String knowledgeBaseId) {
        ThrowUtils.throwIf(StrUtil.isEmptyIfStr(knowledgeBaseId), ResultCode.PARAMS_ERROR, "知识库ID为空");
        String userId = StpUtil.getLoginIdAsString();
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = Wrappers.<KnowledgeBase>lambdaQuery()
                .eq(KnowledgeBase::getKnowledgeBaseId, knowledgeBaseId);
        List<KnowledgeBase> knowledgeBaseList = knowledgeBaseMapper.selectList(queryWrapper);
        ThrowUtils.throwIf(knowledgeBaseList.isEmpty(), ResultCode.NOT_FOUND_ERROR, "不存在的知识库");
        LambdaQueryWrapper<File> fileLambdaQueryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getKnowledgeBaseId, knowledgeBaseId);
        List<File> fileList = fileMapper.selectList(fileLambdaQueryWrapper);
        // 知识库文件为空，直接返回
        if (fileList.isEmpty()) {
            return null;
        }
        // 知识库文件不为空，但是请求者是知识库的拥有者，那么一样全部返回
        List<FileListResponse> fileListResponseList;
        if (knowledgeBaseList.getFirst().getOwnerId().equals(userId)) {
            fileListResponseList = fileList.stream().map(file ->
                    new FileListResponse(
                            file.getFileId(), file.getFileName(), file.getFileSize(), file.getKnowledgeBaseId(),
                            file.getOwnerId(), file.getUploadTime(), file.getIsPublic(), file.getWhitelist(),
                            file.getBlacklist()
                    )
            ).collect(Collectors.toList());
            return fileListResponseList;
        }
        // 用户可能只是该知识库的编辑者或者浏览者，那么我们就要对其进行文件级别的过滤了
        List<File> canAccessFileList = new ArrayList<>();
        for (File file : fileList) {
            if (this.canAccess(file, userId)) {
                canAccessFileList.add(file);
                // 敏感数据脱敏，对于文件的公开权限，非文件主人不得访问
                if (!file.getOwnerId().equals(userId)) {
                    canAccessFileList.getLast().setIsPublic(null);
                    canAccessFileList.getLast().setBlacklist(null);
                    canAccessFileList.getLast().setWhitelist(null);
                }
            }
        }
        fileListResponseList = fileList.stream().map(file ->
                new FileListResponse(
                        file.getFileId(), file.getFileName(), file.getFileSize(), file.getKnowledgeBaseId(),
                        file.getOwnerId(), file.getUploadTime(), file.getIsPublic(), file.getWhitelist(),
                        file.getBlacklist()
                )
        ).collect(Collectors.toList());
        return fileListResponseList;
    }

    @Override
    public String uploadFileToKnowledgeBase(MultipartFile file, String knowledgeBaseId) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ResultCode.PARAMS_ERROR, "无法检测到文件");
        ThrowUtils.throwIf( StrUtil.isEmptyIfStr(knowledgeBaseId), ResultCode.PARAMS_ERROR, "knowledgeBaseId为空");
        log.info("用户正在上传文件{}，到知识库{}", file.getOriginalFilename(), knowledgeBaseId);
        File fileInfo = new File();
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getKnowledgeBaseId, knowledgeBaseId);
        queryWrapper.eq(File::getFileName, file.getOriginalFilename());
        long RepeatedNameFile = fileMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(RepeatedNameFile != 0, ResultCode.PARAMS_ERROR, "已经存在重名文件");
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setOwnerId(StpUtil.getLoginIdAsString());
        fileInfo.setKnowledgeBaseId(knowledgeBaseId);
        // 文件刚刚完成上传，暂时不可见
        fileInfo.setIsPublic(0);
        String url = this.uploadFileToMinIO(file);
        ThrowUtils.throwIf(StringUtils.isBlank(url), ResultCode.OPERATION_ERROR, "文件url为空");
        fileInfo.setUrl(url);
        int count = fileMapper.insert(fileInfo);
        ThrowUtils.throwIf(count != 1, ResultCode.OPERATION_ERROR, "文件内容无法上传到数据库");
        return fileInfo.getFileId();
    }

    @Override
    public String uploadFileToMinIO(MultipartFile file) {
        try {
            return minioService.upload(file);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.OPERATION_ERROR, "上传文件出现了不可预知的错误");
        }
    }


    /*
        注意：这里的权限校验无法进行对于知识库owner可以无视黑白名单的校验
     */
    @Override
    public Boolean canAccess(File file, String userId) {
        // 黑名单最高优先级
        if (StringUtils.isNotBlank(file.getBlacklist()) &&
                Arrays.asList(file.getBlacklist().split(",")).contains(userId)) {
            return false;
        }
        if (file.getIsPublic() != null && file.getIsPublic() == 1) {
            // 只要不是黑名单，所有人都可访问
            return true;
        }
        // 除owner外白名单允许
        if (userId.equals(file.getOwnerId())) {
            return true;
        }
        if (StringUtils.isBlank(file.getWhitelist())) {
            return false;
        }
        return Arrays.asList(file.getWhitelist().split(",")).contains(userId);
    }

}

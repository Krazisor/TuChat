package com.thr.tuchat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.exception.BusinessException;
import com.thr.tuchat.exception.ResultCode;
import com.thr.tuchat.mapper.FileMapper;
import com.thr.tuchat.model.entity.File;
import com.thr.tuchat.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;


@Service
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Resource
    private FileMapper fileMapper;

    @Resource
    private MinioService minioService;

    public Boolean saveFileToKnowledgeBase(MultipartFile file, String knowledgeBaseId) {
        File fileInfo = new File();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setOwnerId(StpUtil.getLoginIdAsString());
        fileInfo.setKnowledgeBaseId(knowledgeBaseId);
        return fileMapper.insert(fileInfo) == 1;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            return minioService.upload(file);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.OPERATION_ERROR, "上传文件出现了不可预知的错误");
        }
    }


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

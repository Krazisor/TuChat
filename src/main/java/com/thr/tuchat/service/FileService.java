package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.model.dto.FileListResponse;
import com.thr.tuchat.model.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<File> {
    List<FileListResponse> getFileListByKnowledgeBaseId(String knowledgeBaseId);

    List<FileListResponse> transferFileToFileListResponse(List<File> fileList);

    String uploadFileToKnowledgeBase(MultipartFile file, String knowledgeBaseId);

    Boolean canAccess(File file, String userId);
}

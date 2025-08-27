package com.thr.tuchat.model.dto;

import java.sql.Timestamp;

public record FileListResponse(
        String fileId,
        String fileName,
        Long fileSize,
        String knowledgeBaseId,
        String ownerId,
        Timestamp uploadTime,
        Integer isPublic,
        String WhiteList,
        String BlackList
) {
}

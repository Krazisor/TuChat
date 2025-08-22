package com.thr.tuchat.model.dto;

import com.thr.tuchat.constant.RoleEnum;

import java.sql.Timestamp;

public record KnowledgeBaseListResponse(
        String knowledgeBaseId,
        String name,
        String description,
        Timestamp createTime,
        RoleEnum role,
        Timestamp joinTime
) {
}

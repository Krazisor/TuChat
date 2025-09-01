package com.thr.tuchat.model.dto;

import com.thr.tuchat.constant.RoleEnum;

public record KnowledgeBaseReOwnerRequest(
        String knowledgeBaseId,
        String newOwnerId,
        RoleEnum newRole
) {
}

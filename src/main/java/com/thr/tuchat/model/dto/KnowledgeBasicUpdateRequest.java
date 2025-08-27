package com.thr.tuchat.model.dto;

public record KnowledgeBasicUpdateRequest(
        String knowledgeBaseId,
        String name,
        String description
) {
}

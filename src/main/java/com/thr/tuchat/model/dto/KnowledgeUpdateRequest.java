package com.thr.tuchat.model.dto;

import java.util.List;

public record KnowledgeUpdateRequest(
        String knowledgeBaseId,
        String name,
        String description,
        List<String> editorList,
        List<String> viewerList
) {
}

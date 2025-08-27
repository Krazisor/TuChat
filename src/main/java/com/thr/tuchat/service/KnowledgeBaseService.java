package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.model.dto.KnowledgeBaseListResponse;
import com.thr.tuchat.model.entity.KnowledgeBase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    String addNewKnowledgeBase(String name, String description, String owner_Id);

    List<KnowledgeBaseListResponse> getKnowledgeBaseList(String ownerId);

    @Transactional(rollbackFor = Exception.class)
    Boolean deleteKnowledgeBaseWithFile(String knowledgeBaseId);
}

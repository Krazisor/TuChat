package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.constant.RoleEnum;
import com.thr.tuchat.model.dto.KnowledgeBaseListResponse;
import com.thr.tuchat.model.entity.KnowledgeBase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    String addNewKnowledgeBase(String name, String description, String owner_Id);

    List<KnowledgeBaseListResponse> getKnowledgeBaseList(String ownerId);

    Map<RoleEnum, List<String>> getKnowledgeBaseMember(String knowledgeBaseId);

    @Transactional(rollbackFor = Exception.class)
    Boolean deleteKnowledgeBaseWithFile(String knowledgeBaseId);

    @Transactional(rollbackFor = Exception.class)
    Boolean updateKnowledgeBaseInfo(
            String knowledgeBaseId, String name, String description,
            List<String> editorList, List<String> viewList);

    boolean changeKnowledgeBaseOwner(String knowledgeBaseId, String newOwnerId, RoleEnum newRole);
}

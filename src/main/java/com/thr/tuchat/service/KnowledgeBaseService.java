package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.model.entity.KnowledgeBase;

public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    Boolean addNewKnowledgeBase(String name, String description, String owner_Id);
}

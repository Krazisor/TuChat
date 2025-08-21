package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.model.entity.File;

public interface FileService extends IService<File> {
    boolean canAccess(File file, String userId);
}

package com.thr.tuchat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.tuchat.model.entity.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<File> {
    String uploadFile (MultipartFile file);

    Boolean canAccess(File file, String userId);
}

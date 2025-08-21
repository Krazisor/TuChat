package com.thr.tuchat.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.tuchat.mapper.FileMapper;
import com.thr.tuchat.model.entity.File;
import com.thr.tuchat.service.FileService;

import java.util.Arrays;

public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {


    @Override
    public boolean canAccess(File file, String userId) {
        // 黑名单最高优先级
        if (StringUtils.isNotBlank(file.getBlacklist()) &&
                Arrays.asList(file.getBlacklist().split(",")).contains(userId)) {
            return false;
        }
        if (file.getIsPublic() != null && file.getIsPublic() == 1) {
            // 只要不是黑名单，所有人都可访问
            return true;
        }
        // 除owner外白名单允许
        if (userId.equals(file.getOwnerId())) {
            return true;
        }
        if (StringUtils.isBlank(file.getWhitelist())) {
            return false;
        }
        return Arrays.asList(file.getWhitelist().split(",")).contains(userId);
    }

}

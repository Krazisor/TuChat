package com.thr.tuchat.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("file")
public class File implements Serializable {

    // 文件ID（主键）
    @TableId(value = "file_id", type = IdType.ASSIGN_UUID)
    private String fileId;

    // 文件名称
    @TableField("file_name")
    private String fileName;

    // 文件存放URL
    @TableField("url")
    private String url;

    // 所属知识库ID
    @TableField("knowledge_base_id")
    private String knowledgeBaseId;

    // 上传用户ID
    @TableField("owner_id")
    private String ownerId;

    // 上传时间（数据库timestamp类型）
    @TableField(value = "upload_time",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private Timestamp uploadTime;

    // 文件大小（字节）
    @TableField("file_size")
    private Long fileSize;

    // 是否公开 1=公开 0=非公开
    @TableField("is_public")
    private Integer isPublic;

    // 允许访问的用户ID（逗号分隔），为空则只有owner可访问
    @TableField("whitelist")
    private String whitelist;

    // 禁止访问的用户ID（逗号分隔）
    @TableField("blacklist")
    private String blacklist;
}
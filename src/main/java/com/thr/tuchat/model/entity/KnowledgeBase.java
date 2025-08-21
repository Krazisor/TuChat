package com.thr.tuchat.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("knowledge_base")
public class KnowledgeBase {

    // 知识库ID（主键）
    @TableId(value = "knowledge_base_id", type = IdType.ASSIGN_UUID)
    private String knowledgeBaseId;

    // 知识库名称
    @TableField("name")
    private String name;

    // 知识库描述
    @TableField("description")
    private String description;

    // 拥有者用户ID
    @TableField("owner_id")
    private String ownerId;

    // 创建时间（数据库timestamp类型）
    @TableField(value = "create_time",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private Timestamp createTime;
}

package com.thr.tuchat.model.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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
@TableName("conversation")
public class Conversation {

    // 主键使用 MyBatis-Plus 内置的随机 UUID（String）
    @TableId(value = "conversation_id", type = IdType.ASSIGN_UUID)
    private String conversationId;

    @TableField("user_id")
    private String userId;

    private String title;

    // 数据库已设置默认当前时间：让 MP 不插入该字段，走 DB 默认值
    @TableField(value = "create_time",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private Timestamp createTime;

    @TableField("is_marked")
    private Boolean isMarked;
}

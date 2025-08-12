package com.thr.tuchat.model.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("message")
public class Message {

    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer messageId;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("role")
    private String role;

    // TEXT 字段，用 String 映射即可
    private String content;

    // 使用数据库默认 CURRENT_TIMESTAMP，不由应用赋值
    @TableField(value = "create_time",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private Timestamp createTime;

    @TableField("error_message")
    private String errorMessage;

    private String attachment;
}

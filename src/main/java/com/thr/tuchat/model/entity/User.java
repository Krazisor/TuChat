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
@TableName("`user`") // user 是 MySQL 关键字，使用反引号避免冲突
public class User {

    @TableId(value = "user_id")
    private String userId;

    @TableField("user_name")
    private String userName;

    private String email;

    // 由数据库默认 CURRENT_TIMESTAMP 填充，不在插入/更新时由应用赋值
    @TableField(value = "create_time",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private Timestamp createTime;

    private String avatar;
}
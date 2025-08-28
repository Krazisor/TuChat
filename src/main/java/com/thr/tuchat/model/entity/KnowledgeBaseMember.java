package com.thr.tuchat.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.thr.tuchat.constant.RoleEnum;
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
@TableName("knowledge_base_member")
public class KnowledgeBaseMember implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("knowledge_base_id")
    private String knowledgeBaseId;

    @TableField("user_id")
    private String userId;

    /**
     * 权限角色：owner, editor, VIEWER
     */
    @TableField("role")
    private RoleEnum role;

    @TableField(value = "join_time", fill = FieldFill.INSERT)
    private Timestamp joinTime;
}

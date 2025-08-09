CREATE DATABASE IF NOT EXISTS tu_chat;

USE tu_chat;

CREATE TABLE conversation
(
    conversation_id VARCHAR(64) PRIMARY KEY COMMENT '会话ID',
    user_id         VARCHAR(64)  NOT NULL COMMENT '用户ID',
    title           VARCHAR(255) NOT NULL COMMENT '会话标题',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_marked       BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否标记'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='会话表';

CREATE TABLE message
(
    message_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '所属会话ID',
    role            VARCHAR(32) NOT NULL COMMENT '发送方身份',
    content         TEXT COMMENT '消息内容',
    create_time     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    error_message   VARCHAR(255)         DEFAULT NULL COMMENT '错误信息',
    attachment      VARCHAR(512)         DEFAULT NULL COMMENT '附件链接/信息',
    KEY idx_conversation_id (conversation_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息表';

CREATE TABLE user
(
    user_id     VARCHAR(64) PRIMARY KEY COMMENT '用户ID',
    user_name   VARCHAR(64)  NOT NULL COMMENT '用户名',
    email       VARCHAR(128) NOT NULL UNIQUE COMMENT '邮箱',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    avatar      VARCHAR(255)          DEFAULT NULL COMMENT '头像地址'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

CREATE TABLE knowledge_base
(
    knowledge_base_id VARCHAR(64) PRIMARY KEY COMMENT '知识库ID',
    name              VARCHAR(128) NOT NULL COMMENT '知识库名称',
    description       TEXT                  DEFAULT NULL COMMENT '知识库描述',
    owner_id          VARCHAR(64)  NOT NULL COMMENT '拥有者用户ID',
    create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='知识库表';

CREATE TABLE file
(
    file_id           VARCHAR(64) PRIMARY KEY COMMENT '文件ID',
    file_name         VARCHAR(255) NOT NULL COMMENT '文件名称',
    url               VARCHAR(512) NOT NULL COMMENT '文件存放URL',
    knowledge_base_id VARCHAR(64)  NOT NULL COMMENT '所属知识库ID',
    owner_id          VARCHAR(64)  NOT NULL COMMENT '上传用户ID',
    upload_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文件表';

CREATE TABLE knowledge_base_member
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    knowledge_base_id VARCHAR(64)                        NOT NULL COMMENT '知识库ID',
    user_id           VARCHAR(64)                        NOT NULL COMMENT '用户ID',
    role              ENUM ('owner', 'editor', 'viewer') NOT NULL COMMENT '权限角色',
    join_time         TIMESTAMP                          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uniq_kb_user (knowledge_base_id, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='知识库成员权限表';


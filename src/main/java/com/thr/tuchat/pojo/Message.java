package com.thr.tuchat.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Integer messageId;
    private String conversationId;
    private String role;
    private String content;
    private Timestamp createTime;
    private String errorMessage;
    private String attachment;
}

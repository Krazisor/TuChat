package com.thr.tuchat.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    private String conversationId;
    private String userId;
    private String title;
    private Timestamp createTime;
    private Boolean isMarked;
}

package com.thr.tuchat.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIRequestDTO {
    private String conversationId;
    private String question;
    private String model;
}

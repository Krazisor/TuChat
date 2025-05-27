package com.thr.tuchat.constant;

import lombok.Getter;

@Getter
public enum AIMessageType {
    USER ("user"),
    SYS("system"),
    ASSISTANT ("assistant"),
    TOOL ("tool");

    private final String role;

    AIMessageType(String role) {
        this.role = role;
    }
}

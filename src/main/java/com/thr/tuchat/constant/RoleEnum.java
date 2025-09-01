package com.thr.tuchat.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RoleEnum {
    OWNER("owner"),
    EDITOR("editor"),
    VIEWER("viewer"),
    NONE("none");

    @EnumValue
    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }

}

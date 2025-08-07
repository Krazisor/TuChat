package com.thr.tuchat.constant;


import lombok.Getter;

@Getter
public enum DocumentPermissionType {

    PUBLIC("public"),
    PRIVATE("private"),
    WHITE("whiteList"),
    BLACK("blackList");

    private final String permissionType;

    DocumentPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }
}

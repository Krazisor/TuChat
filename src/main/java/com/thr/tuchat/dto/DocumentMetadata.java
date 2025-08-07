package com.thr.tuchat.dto;


import com.thr.tuchat.constant.DocumentPermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentMetadata {

    private String userId;

    private DocumentPermissionType permissionType;

    private String allowList;

    private String denyList;


}

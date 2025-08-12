package com.thr.tuchat.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentBaseMetadata {

    private String fileId;

    private String fileName;

    private String userId;

    private Boolean isTemporaryFile;

    private String temporarySessionId;

}

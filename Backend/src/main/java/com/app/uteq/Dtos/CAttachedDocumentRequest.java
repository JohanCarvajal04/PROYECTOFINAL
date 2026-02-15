package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CAttachedDocumentRequest {
    private Integer applicationsIdApplication;
    private Integer requirementId;
    private String fileName;
    private String filePath;
    private Long fileSizeBytes;
    private String mimeType;
    private Integer uploadedByUserId;
}

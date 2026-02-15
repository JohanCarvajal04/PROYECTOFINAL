package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UAttachedDocumentRequest {
    private Integer idAttachedDocument;
    private Integer applicationsIdApplication;
    private Integer requirementId;
    private String fileName;
    private String filePath;
    private Long fileSizeBytes;
    private String mimeType;
    private Integer uploadedByUserId;
}

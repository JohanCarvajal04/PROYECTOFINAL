package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UDocumentGeneratedRequest {
    private Integer idDocumentGenerated;
    private Integer applicationsIdApplication;
    private Integer templateId;
    private String documentType;
    private String documentPath;
    private Integer generatedByUserId;
    private Integer digitalSignatureId;
}

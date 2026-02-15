package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentGeneratedResponse {
    private Integer idDocumentGenerated;
    private Integer applicationsIdApplication;
    private Integer templateId;
    private String documentType;
    private String documentPath;
    private LocalDateTime generatedAt;
    private Integer generatedByUserId;
    private Integer digitalSignatureId;
    private LocalDateTime signatureTimestamp;
}

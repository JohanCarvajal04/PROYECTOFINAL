package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachedDocumentResponse {
    private Integer idAttachedDocument;
    private Integer applicationsIdApplication;
    private Integer requirementId;
    private String fileName;
    private String filePath;
    private Long fileSizeBytes;
    private String mimeType;
    private LocalDateTime uploadDate;
    private Integer uploadedByUserId;
}

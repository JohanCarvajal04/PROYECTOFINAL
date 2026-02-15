package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResponse {
    private Integer idApplication;
    private String applicationCode;
    private LocalDateTime creationDate;
    private LocalDate estimatedCompletionDate;
    private LocalDateTime actualCompletionDate;
    private String applicationDetails;
    private String applicationResolution;
    private Integer rejectionReasonId;
    private Integer currentStageTrackingId;
    private Integer proceduresIdProcedure;
    private Integer applicantUserId;
    private String priority;
}

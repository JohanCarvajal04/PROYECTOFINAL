package com.app.uteq.Dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CApplicationRequest {
    private String applicationCode;
    private LocalDate estimatedCompletionDate;
    private String applicationDetails;
    private Integer rejectionReasonId;
    private Integer currentStageTrackingId;
    private Integer proceduresIdProcedure;
    private Integer applicantUserId;
    private String priority;
}

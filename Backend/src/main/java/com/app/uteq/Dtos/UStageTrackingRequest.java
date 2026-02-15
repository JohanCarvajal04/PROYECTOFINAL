package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UStageTrackingRequest {
    private Integer idStageTracking;
    private Integer stateIdState;
    private Integer processingStageIdProcessingStage;
    private Integer assignedToUserId;
    private String notes;
}

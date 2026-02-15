package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CStageTrackingRequest {
    private Integer stateIdState;
    private Integer processingStageIdProcessingStage;
    private Integer assignedToUserId;
    private String notes;
}

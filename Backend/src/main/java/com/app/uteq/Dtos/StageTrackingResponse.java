package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StageTrackingResponse {
    private Integer idStageTracking;
    private Integer stateIdState;
    private Integer processingStageIdProcessingStage;
    private LocalDateTime enteredAt;
    private LocalDateTime completedAt;
    private Integer assignedToUserId;
    private String notes;
}

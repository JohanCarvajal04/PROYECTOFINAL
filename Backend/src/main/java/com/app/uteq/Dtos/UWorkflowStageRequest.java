package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UWorkflowStageRequest {
    private Integer idWorkflowStage;
    private Integer workflowIdWorkflow;
    private Integer processingStageIdProcessingStage;
    private Integer sequenceOrder;
    private Boolean isOptional;
}

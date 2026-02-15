package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowStageResponse {
    private Integer idWorkflowStage;
    private Integer workflowIdWorkflow;
    private Integer processingStageIdProcessingStage;
    private Integer sequenceOrder;
    private Boolean isOptional;
}

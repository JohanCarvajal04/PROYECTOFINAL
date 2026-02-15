package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UWorkflowRequest {
    private Integer idworkflow;
    private String workflowname;
    private String workflowdescription;
    private Boolean active;
}

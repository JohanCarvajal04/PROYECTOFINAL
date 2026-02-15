package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CWorkflowRequest {
    private String workflowname;
    private String workflowdescription;
    private Boolean active;
}

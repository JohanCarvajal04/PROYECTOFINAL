package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowResponse {
    private Integer idworkflow;
    private String workflowname;
    private String workflowdescription;
    private LocalDateTime createdat;
    private Boolean active;
}

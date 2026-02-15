package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingStageResponse {
    private Integer idprocessingstage;
    private String stagename;
    private String stagecode;
    private String stagedescription;
    private Integer stageorder;
    private Boolean requiresapproval;
    private Integer maxdurationdays;
}

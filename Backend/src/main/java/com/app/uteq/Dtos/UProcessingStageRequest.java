package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UProcessingStageRequest {
    private Integer idprocessingstage;
    private String stagename;
    private String stagecode;
    private String stagedescription;
    private Integer stageorder;
    private Boolean requiresapproval;
    private Integer maxdurationdays;
}

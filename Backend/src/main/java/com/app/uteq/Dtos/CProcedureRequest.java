package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CProcedureRequest {
    private String procedurename;
    private String description;
    private Integer maxduration;
    private Integer workflowidworkflow;
}

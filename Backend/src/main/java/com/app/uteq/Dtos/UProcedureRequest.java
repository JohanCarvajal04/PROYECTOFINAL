package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UProcedureRequest {
    private Integer idProcedure;
    private String procedurename;
    private String description;
    private Integer maxduration;
    private Integer workflowidworkflow;
    private String status;
}

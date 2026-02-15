package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcedureResponse {
    private Integer idProcedure;
    private String procedurename;
    private String description;
    private Integer maxduration;
    private Integer workflowidworkflow;
    private String status;
    private LocalDateTime createdAt;
}

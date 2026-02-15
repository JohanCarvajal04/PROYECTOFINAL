package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class URequirementRequest {
    private Integer id;
    private Integer proceduresIdProcedure;
    private String requirementName;
    private String requirementDescription;
    private String requirementType;
    private Boolean isMandatory;
    private Integer displayOrder;
}

package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequirementResponse {
    private Integer id;
    private Integer proceduresIdProcedure;
    private String requirementName;
    private String requirementDescription;
    private String requirementType;
    private Boolean isMandatory;
    private Integer displayOrder;
}

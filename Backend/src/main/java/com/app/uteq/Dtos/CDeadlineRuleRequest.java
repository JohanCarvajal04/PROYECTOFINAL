package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CDeadlineRuleRequest {
    private String rulename;
    private String procedurecategory;
    private Integer basedeadlinedays;
    private Integer warningdaysbefore;
    private Boolean active;
}

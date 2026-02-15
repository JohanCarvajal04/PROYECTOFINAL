package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeadlineRuleResponse {
    private Integer iddeadlinerule;
    private String rulename;
    private String procedurecategory;
    private Integer basedeadlinedays;
    private Integer warningdaysbefore;
    private Boolean active;
}

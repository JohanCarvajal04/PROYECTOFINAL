package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateResponse {
    private Integer idstate;
    private String statename;
    private String statedescription;
    private String statecategory;
}

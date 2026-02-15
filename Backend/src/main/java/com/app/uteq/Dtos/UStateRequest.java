package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UStateRequest {
    private Integer idstate;
    private String statename;
    private String statedescription;
    private String statecategory;
}

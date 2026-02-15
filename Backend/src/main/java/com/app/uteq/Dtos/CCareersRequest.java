package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CCareersRequest {
    private String careername;
    private String careercode;
    private Integer facultiesidfaculty;
    private Integer coordinatoriduser; // opcional
}

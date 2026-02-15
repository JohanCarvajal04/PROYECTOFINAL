package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UCareersRequest {
    private Integer idcareer;
    private String careername;
    private String careercode;
    private Integer facultiesidfaculty;
    private Integer coordinatoriduser; // opcional
}

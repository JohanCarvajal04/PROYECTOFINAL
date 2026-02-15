package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CareersResponse {
    private Integer idcareer;
    private String careername;
    private String careercode;
    private Integer facultiesidfaculty;
    private String facultyname;        // viene del JOIN en la función
    private Integer coordinatoriduser; // puede ser null
}

package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CCareersRequest {

    @NotBlank(message = "El nombre de la carrera es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String careername;

    @NotBlank(message = "El código de la carrera es obligatorio")
    @Size(min = 2, max = 20, message = "El código debe tener entre 2 y 20 caracteres")
    private String careercode;

    @NotNull(message = "El ID de la facultad es obligatorio")
    private Integer facultiesidfaculty;

    private Integer coordinatoriduser;
}

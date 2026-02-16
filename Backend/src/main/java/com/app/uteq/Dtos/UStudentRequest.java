package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UStudentRequest {

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Integer idStudent;

    @NotBlank(message = "El semestre es obligatorio")
    @Pattern(regexp = "^([1-9]|10)$", message = "El semestre debe ser un número del 1 al 10")
    private String semester;

    @NotBlank(message = "El paralelo es obligatorio")
    @Pattern(regexp = "^[A-Z]$", message = "El paralelo debe ser una letra mayúscula (A-Z)")
    private String parallel;

    @NotNull(message = "El ID de carrera es obligatorio")
    private Integer careersIdCareer;

    @Pattern(regexp = "^(activo|inactivo|graduado|retirado|suspendido)$", 
             message = "El estado debe ser: activo, inactivo, graduado, retirado o suspendido")
    private String status;
}

package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CStudentRequest {

    @NotBlank(message = "El semestre es requerido")
    @Pattern(regexp = "^[1-9]|10$", message = "El semestre debe ser un número entre 1 y 10")
    private String semester;

    @NotBlank(message = "El paralelo es requerido")
    @Pattern(regexp = "^[A-Z]$", message = "El paralelo debe ser una letra mayúscula (A-Z)")
    private String parallel;

    @NotNull(message = "El usuario es requerido")
    private Integer usersIdUser;

    @NotNull(message = "La carrera es requerida")
    private Integer careersIdCareer;

    @Pattern(regexp = "^(activo|inactivo|graduado|retirado)$", message = "El estado debe ser: activo, inactivo, graduado o retirado")
    private String status;
}

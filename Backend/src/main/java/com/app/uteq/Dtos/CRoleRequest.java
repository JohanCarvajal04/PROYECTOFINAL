package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CRoleRequest {

    @NotBlank(message = "El nombre del rol es requerido")
    @Size(min = 3, max = 50, message = "El nombre del rol debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^ROLE_[A-Z_]+$", message = "El nombre del rol debe iniciar con 'ROLE_' y usar mayúsculas")
    private String roleName;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String roleDescription;
}

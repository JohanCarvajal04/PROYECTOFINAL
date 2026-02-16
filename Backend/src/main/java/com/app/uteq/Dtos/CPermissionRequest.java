package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CPermissionRequest {

    @NotBlank(message = "El código del permiso es obligatorio")
    @Pattern(regexp = "^[A-Z_]+$", message = "El código debe contener solo letras mayúsculas y guiones bajos")
    @Size(min = 3, max = 50, message = "El código debe tener entre 3 y 50 caracteres")
    private String code;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;
}

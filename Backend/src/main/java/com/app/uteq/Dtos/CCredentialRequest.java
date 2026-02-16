package com.app.uteq.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CCredentialRequest {

    @NotBlank(message = "El nombre de usuario es requerido")
    @Email(message = "El nombre de usuario debe ser un correo válido")
    private String username;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "La contraseña debe contener al menos: una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String passwordHash;
}

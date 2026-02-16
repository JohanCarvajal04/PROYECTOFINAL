package com.app.uteq.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UUserRequest {

    @NotNull(message = "El ID de usuario es requerido")
    private Integer idUser;

    @NotBlank(message = "Los nombres son requeridos")
    @Size(min = 2, max = 255, message = "Los nombres deben tener entre 2 y 255 caracteres")
    private String names;

    @NotBlank(message = "Los apellidos son requeridos")
    @Size(min = 2, max = 255, message = "Los apellidos deben tener entre 2 y 255 caracteres")
    private String surnames;

    @NotBlank(message = "La cédula es requerida")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe tener exactamente 10 dígitos")
    private String cardId;

    @NotBlank(message = "El correo institucional es requerido")
    @Email(message = "El correo institucional debe ser válido")
    private String institutionalEmail;

    @Email(message = "El correo personal debe ser válido")
    private String personalMail;

    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    private String phoneNumber;

    private Boolean statement;

    @NotNull(message = "La configuración es requerida")
    private Integer configurationsIdConfiguration;

    private Boolean active;
}

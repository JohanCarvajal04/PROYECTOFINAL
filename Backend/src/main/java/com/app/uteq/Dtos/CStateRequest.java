package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CStateRequest {

    @NotBlank(message = "El nombre del estado es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String statename;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String statedescription;

    @NotBlank(message = "La categoría del estado es obligatoria")
    private String statecategory;
}

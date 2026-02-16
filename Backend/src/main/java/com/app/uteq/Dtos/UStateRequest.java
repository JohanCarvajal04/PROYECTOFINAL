package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UStateRequest {

    @NotNull(message = "El ID del estado es obligatorio")
    private Integer idstate;

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String statename;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String statedescription;

    private String statecategory;
}

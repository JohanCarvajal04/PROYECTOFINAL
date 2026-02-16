package com.app.uteq.Dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CProcedureRequest {

    @NotBlank(message = "El nombre del trámite es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String procedurename;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    @NotNull(message = "La duración máxima es requerida")
    @Min(value = 1, message = "La duración máxima debe ser al menos 1 día")
    @Max(value = 365, message = "La duración máxima no puede exceder 365 días")
    private Integer maxduration;

    @NotNull(message = "El flujo de trabajo es requerido")
    private Integer workflowidworkflow;
}

package com.app.uteq.Dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CProcessingStageRequest {

    @NotBlank(message = "El nombre de la etapa es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String stagename;

    @NotBlank(message = "El código de la etapa es obligatorio")
    @Size(min = 2, max = 50, message = "El código debe tener entre 2 y 50 caracteres")
    private String stagecode;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String stagedescription;

    @NotNull(message = "El orden de la etapa es obligatorio")
    @Min(value = 1, message = "El orden debe ser mayor o igual a 1")
    private Integer stageorder;

    private Boolean requiresapproval;

    @Min(value = 1, message = "La duración máxima debe ser al menos 1 día")
    private Integer maxdurationdays;
}

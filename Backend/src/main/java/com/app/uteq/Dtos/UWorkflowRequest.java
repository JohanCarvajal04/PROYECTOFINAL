package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UWorkflowRequest {

    @NotNull(message = "El ID del workflow es obligatorio")
    private Integer idworkflow;

    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String workflowname;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String workflowdescription;

    private Boolean active;
}

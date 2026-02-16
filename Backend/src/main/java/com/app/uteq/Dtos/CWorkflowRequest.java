package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CWorkflowRequest {

    @NotBlank(message = "El nombre del workflow es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String workflowname;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String workflowdescription;

    private Boolean active;
}

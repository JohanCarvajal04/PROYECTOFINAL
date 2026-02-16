package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CRejectionReasonRequest {

    @NotBlank(message = "El código de la razón es obligatorio")
    @Size(min = 2, max = 50, message = "El código debe tener entre 2 y 50 caracteres")
    private String reasoncode;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres")
    private String reasondescription;

    @NotBlank(message = "La categoría es obligatoria")
    private String category;

    private Boolean active;
}

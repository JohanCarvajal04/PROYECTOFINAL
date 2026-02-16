package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CDocumentTemplateRequest {

    @NotBlank(message = "El nombre del template es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String templatename;

    @NotBlank(message = "El código del template es obligatorio")
    @Size(min = 2, max = 50, message = "El código debe tener entre 2 y 50 caracteres")
    private String templatecode;

    private String templatepath;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String documenttype;

    private String version;

    private Boolean requiressignature;

    private Boolean active;
}

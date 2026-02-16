package com.app.uteq.Dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CApplicationRequest {

    @NotBlank(message = "El código de solicitud es requerido")
    @Size(max = 100, message = "El código no puede exceder 100 caracteres")
    private String applicationCode;

    @NotNull(message = "La fecha estimada de completación es requerida")
    @FutureOrPresent(message = "La fecha estimada debe ser hoy o en el futuro")
    private LocalDate estimatedCompletionDate;

    @Size(max = 5000, message = "Los detalles no pueden exceder 5000 caracteres")
    private String applicationDetails;

    private Integer rejectionReasonId;

    @NotNull(message = "El seguimiento de etapa actual es requerido")
    private Integer currentStageTrackingId;

    @NotNull(message = "El procedimiento es requerido")
    private Integer proceduresIdProcedure;

    @NotNull(message = "El usuario solicitante es requerido")
    private Integer applicantUserId;

    @Pattern(regexp = "^(baja|normal|alta|urgente)$", message = "La prioridad debe ser: baja, normal, alta o urgente")
    private String priority;
}

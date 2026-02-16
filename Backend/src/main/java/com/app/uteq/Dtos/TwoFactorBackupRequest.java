package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorBackupRequest {

    @NotBlank(message = "El código de respaldo es requerido")
    private String backupCode;
}

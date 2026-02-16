package com.app.uteq.Dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TwoFactorVerifyRequest {

    @NotNull(message = "El código TOTP es requerido")
    private Integer code;
}

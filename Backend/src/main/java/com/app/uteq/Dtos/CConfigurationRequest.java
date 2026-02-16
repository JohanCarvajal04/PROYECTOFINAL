package com.app.uteq.Dtos;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CConfigurationRequest {

    private String profilepicturepath;

    private String signaturepath;

    private Boolean enable_sms;

    private Boolean enable_email;

    private Boolean enable_whatsapp;

    @Pattern(regexp = "^(diaria|semanal|mensual|inmediata)$", 
             message = "La frecuencia debe ser: diaria, semanal, mensual o inmediata")
    private String notificationfrequency;
}

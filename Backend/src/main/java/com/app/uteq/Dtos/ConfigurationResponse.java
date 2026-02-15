package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationResponse {
    private Integer idconfiguration;
    private String profilepicturepath;
    private String signaturepath;
    private Boolean enable_sms;
    private Boolean enable_email;
    private Boolean enable_whatsapp;
    private String notificationfrequency;
}

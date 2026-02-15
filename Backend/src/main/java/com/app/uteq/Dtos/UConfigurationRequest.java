package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UConfigurationRequest {
    private Integer idconfiguration;
    private String profilepicturepath;
    private String signaturepath;
    private Boolean enable_sms;
    private Boolean enable_email;
    private Boolean enable_whatsapp;
    private String notificationfrequency;
}

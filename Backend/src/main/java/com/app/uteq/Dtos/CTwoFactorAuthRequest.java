package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CTwoFactorAuthRequest {
    private Integer credentialsIdCredentials;
    private Boolean enabled;
    private String secretKey;
}

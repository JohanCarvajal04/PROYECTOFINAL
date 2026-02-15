package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UTwoFactorAuthRequest {
    private Integer id2fa;
    private Integer credentialsIdCredentials;
    private Boolean enabled;
    private String secretKey;
}

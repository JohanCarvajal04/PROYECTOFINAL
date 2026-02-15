package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorAuthResponse {
    private Integer id2fa;
    private Integer credentialsIdCredentials;
    private Boolean enabled;
    private String secretKey;
    private LocalDateTime verifiedAt;
}

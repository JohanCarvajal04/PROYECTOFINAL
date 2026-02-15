package com.app.uteq.Dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class URefreshTokenRequest {
    private Integer id;
    private Integer userId;
    private String token;
    private LocalDateTime expiresAt;
    private Boolean revoked;
}

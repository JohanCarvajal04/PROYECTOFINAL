package com.app.uteq.Dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CRefreshTokenRequest {
    private Integer userId;
    private String token;
    private LocalDateTime expiresAt;
}

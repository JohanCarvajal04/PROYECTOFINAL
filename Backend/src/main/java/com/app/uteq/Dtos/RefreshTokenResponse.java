package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponse {
    private Integer id;
    private Integer userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Boolean revoked;
}

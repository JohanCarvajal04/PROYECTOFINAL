package com.app.uteq.Dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponse {
    private Long id;
    private Integer userId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Boolean revoked;
    private String deviceInfo;
}

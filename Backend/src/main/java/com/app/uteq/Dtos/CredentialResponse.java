package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CredentialResponse {
    private Integer idCredentials;
    private String username;
    private LocalDateTime lastLogin;
    private Integer failedAttempts;
    private Boolean accountLocked;
    private LocalDate passwordExpiryDate;
}

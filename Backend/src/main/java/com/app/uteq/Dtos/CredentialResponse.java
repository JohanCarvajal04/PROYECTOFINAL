package com.app.uteq.Dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Boolean active;
}

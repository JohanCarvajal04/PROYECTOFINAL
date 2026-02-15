package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class UCredentialRequest {
    private Integer idCredentials;
    private String username;
    private String passwordHash;
}

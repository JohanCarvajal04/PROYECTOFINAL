package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CCredentialRequest {
    private String username;
    private String passwordHash;
}

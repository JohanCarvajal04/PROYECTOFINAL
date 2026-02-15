package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class USessionTokenRequest {
    private Integer idSession;
    private Integer userId;
    private String token;
    private String ipAddress;
    private String userAgent;
}

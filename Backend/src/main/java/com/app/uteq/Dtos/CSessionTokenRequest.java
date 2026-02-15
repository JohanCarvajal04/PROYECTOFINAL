package com.app.uteq.Dtos;

import lombok.Data;

@Data
public class CSessionTokenRequest {
    private Integer userId;
    private String token;
    private String ipAddress;
    private String userAgent;
}

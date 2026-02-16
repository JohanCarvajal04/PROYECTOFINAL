package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UTwoFactorAuthRequest {

    private String secretKey;
    private String qrCodeUri;
    private List<String> backupCodes;
}

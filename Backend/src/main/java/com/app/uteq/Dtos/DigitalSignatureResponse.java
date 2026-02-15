package com.app.uteq.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DigitalSignatureResponse {
    private Integer idDigitalSignature;
    private Integer userIdUser;
    private String certificatePath;
    private String certificateSerial;
    private String issuer;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private String signatureAlgorithm;
    private Boolean active;
    private LocalDateTime createdAt;
}

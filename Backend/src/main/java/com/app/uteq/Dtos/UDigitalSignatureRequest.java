package com.app.uteq.Dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UDigitalSignatureRequest {
    private Integer idDigitalSignature;
    private Integer userIdUser;
    private String certificatePath;
    private String certificateSerial;
    private String issuer;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private String signatureAlgorithm;
    private Boolean active;
}

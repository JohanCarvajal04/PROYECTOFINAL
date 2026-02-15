package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "digitalsignatures")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DigitalSignatures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddigitalsignature")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "useriduser", nullable = false)
    private Users user;

    @Column(name = "certificatepath", nullable = false, length = 500)
    private String certificatePath;

    @Column(name = "certificateserial", nullable = false, unique = true, length = 255)
    private String certificateSerial;

    @Column(name = "issuer", nullable = false, length = 255)
    private String issuer;

    @Column(name = "validfrom", nullable = false)
    private LocalDate validFrom;

    @Column(name = "validuntil", nullable = false)
    private LocalDate validUntil;

    @Column(name = "signaturealgorithm", nullable = false, length = 100)
    private String signatureAlgorithm;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;
}

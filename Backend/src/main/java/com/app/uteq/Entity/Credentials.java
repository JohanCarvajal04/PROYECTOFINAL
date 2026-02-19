package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "credentials")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Credentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcredentials")
    private Integer idCredentials;

    @Column(name = "passwordhash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "datemodification")
    private LocalDateTime dateModification;

    @Column(name = "lastlogin")
    private LocalDateTime lastLogin;

    @Column(name = "failedattempts", nullable = false)
    private Integer failedAttempts = 0;

    @Column(name = "accountlocked", nullable = false)
    private Boolean accountLocked = false;

    @Column(name = "passwordexpirydate")
    private LocalDate passwordExpiryDate;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}

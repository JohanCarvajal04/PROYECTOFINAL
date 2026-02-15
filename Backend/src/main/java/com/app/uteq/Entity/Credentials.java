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
@Table(name = "credentials")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Credentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcredentials")
    private Integer id;

    @Column(name = "passwordhash", nullable = false)
    private String passwordHash;

    @Column(name = "datemodification")
    private LocalDateTime dateModification;

    @Column(name = "lastlogin")
    private LocalDateTime lastLogin;

    @Column(name = "failedattempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;

    @Column(name = "accountlocked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "passwordexpirydate")
    private LocalDate passwordExpiryDate;

}

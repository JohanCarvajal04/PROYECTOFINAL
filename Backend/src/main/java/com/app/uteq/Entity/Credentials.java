package com.app.uteq.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

}

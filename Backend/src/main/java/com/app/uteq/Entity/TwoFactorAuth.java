package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "twofactorauth")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwoFactorAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id2fa")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "credentialsidcredentials", nullable = false, unique = true)
    private Credentials credentials;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "secretkey", columnDefinition = "TEXT")
    private String secretKey;

    @Column(name = "backupcodes", columnDefinition = "TEXT[]")
    private List<String> backupCodes;

    @Column(name = "verifiedat")
    private LocalDateTime verifiedAt;
}

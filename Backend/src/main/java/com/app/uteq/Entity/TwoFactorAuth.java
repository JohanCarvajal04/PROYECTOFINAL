package com.app.uteq.Entity;

import java.time.LocalDateTime;
import java.util.List;

import com.app.uteq.Config.StringListConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Convert(converter = StringListConverter.class)
    @Column(name = "backupcodes", columnDefinition = "TEXT")
    private List<String> backupCodes;

    @Column(name = "verifiedat")
    private LocalDateTime verifiedAt;
}

package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "sessiontokens")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idsession")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false)
    private Users user;

    @Column(name = "token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(name = "ipaddress", length = 45)
    private String ipAddress;

    @Column(name = "useragent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiresat", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "lastactivity")
    private LocalDateTime lastActivity;
}

package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "credentials")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Credencials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcredentials;

    @Column(nullable = false)
    private String passwordhash;

    private Integer failedattempts = 0;
    private Boolean accountlocked = false;
    private Boolean active = true;

    @OneToOne(mappedBy = "credentials")
    private Users user;
}

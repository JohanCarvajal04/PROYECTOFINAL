package com.app.uteq.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String ipaddress;
    private String useragent;
    private LocalDateTime expiresat;
    private Boolean isactive = true;
}

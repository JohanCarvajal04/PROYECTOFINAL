package com.app.uteq.Entity;

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
@Table(name = "configurations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Configuration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idconfiguration;

    private String profilepicturepath;
    private String theme; // Ej: "dark", "light"
    private String language;

    @Column(name = "enable_email")
    private Boolean enableEmail = true;

    @Column(name = "enable_sms")
    private Boolean enableSms = false;

    @Column(name = "notifications")
    private Boolean notifications = true;
}

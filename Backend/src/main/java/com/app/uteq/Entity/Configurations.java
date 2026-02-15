package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "configurations")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Configurations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idconfiguration")
    private Integer id;

    @Column(name = "profilepicturepath", length = 500)
    private String profilePicturePath;

    @Column(name = "signaturepath", length = 500)
    private String signaturePath;

    @Column(name = "enable_sms", nullable = false)
    @Builder.Default
    private Boolean enableSms = false;

    @Column(name = "enable_email", nullable = false)
    @Builder.Default
    private Boolean enableEmail = true;

    @Column(name = "enable_whatsapp", nullable = false)
    @Builder.Default
    private Boolean enableWhatsapp = false;

    @Column(name = "notificationfrequency", length = 50, nullable = false)
    @Builder.Default
    private String notificationFrequency = "real_time";
}

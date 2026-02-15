package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documenttemplates")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentTemplates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtemplate")
    private Integer idTemplate;

    @Column(name = "templatename", nullable = false, length = 255)
    private String templateName;

    @Column(name = "templatecode", nullable = false, length = 50, unique = true)
    private String templateCode;

    @Column(name = "templatepath", nullable = false, length = 500)
    private String templatePath;

    @Column(name = "documenttype", nullable = false, length = 100)
    private String documentType;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "requiressignature", nullable = false)
    @Builder.Default
    private Boolean requiresSignature = false;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}

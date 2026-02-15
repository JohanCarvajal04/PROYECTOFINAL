package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "documentsgenerated")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentsGenerated {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddocumentgenerated")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "applicationsidapplication", nullable = false)
    private Applications application;

    @ManyToOne
    @JoinColumn(name = "templateid")
    private DocumentTemplates template;

    @Column(name = "documenttype", nullable = false, length = 255)
    private String documentType;

    @Column(name = "documentpath", nullable = false, length = 500)
    private String documentPath;

    @Column(name = "generatedat", nullable = false)
    private LocalDateTime generatedAt;

    @ManyToOne
    @JoinColumn(name = "generatedbyuserid", nullable = false)
    private Users generatedByUser;

    @ManyToOne
    @JoinColumn(name = "digitalsignatureid")
    private DigitalSignatures digitalSignature;

    @Column(name = "signaturetimestamp")
    private LocalDateTime signatureTimestamp;
}

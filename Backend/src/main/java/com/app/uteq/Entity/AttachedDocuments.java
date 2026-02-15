package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "attacheddocuments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachedDocuments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idattacheddocument")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "applicationsidapplication", nullable = false)
    private Applications application;

    @ManyToOne
    @JoinColumn(name = "requirementid")
    private RequirementsOfTheProcedure requirement;

    @Column(name = "filename", nullable = false, length = 255)
    private String fileName;

    @Column(name = "filepath", nullable = false, length = 500)
    private String filePath;

    @Column(name = "filesizebytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mimetype", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "uploaddate", nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "uploadedbyuserid", nullable = false)
    private Users uploadedByUser;
}

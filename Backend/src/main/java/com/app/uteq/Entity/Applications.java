package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "applications")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Applications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idapplication")
    private Integer id;

    @Column(name = "applicationcode", nullable = false, unique = true, length = 100)
    private String applicationCode;

    @Column(name = "creationdate", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "estimatedcompletiondate", nullable = false)
    private LocalDate estimatedCompletionDate;

    @Column(name = "actualcompletiondate")
    private LocalDateTime actualCompletionDate;

    @Column(name = "applicationdetails", columnDefinition = "TEXT")
    private String applicationDetails;

    @Column(name = "applicationresolution", columnDefinition = "TEXT")
    private String applicationResolution;

    @ManyToOne
    @JoinColumn(name = "rejectionreasonid")
    private RejectionReasons rejectionReason;

    @ManyToOne
    @JoinColumn(name = "currentstagetrackingid", nullable = false)
    private StageTracking currentStageTracking;

    @ManyToOne
    @JoinColumn(name = "proceduresidprocedure", nullable = false)
    private Procedures procedure;

    @ManyToOne
    @JoinColumn(name = "applicantuserid", nullable = false)
    private Users applicantUser;

    @Column(name = "priority", length = 20, nullable = false)
    @Builder.Default
    private String priority = "normal";
}

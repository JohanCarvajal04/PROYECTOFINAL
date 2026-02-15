package com.app.uteq.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Procedures {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idprocedure")
    private Integer idProcedure;

    @Column(name = "nameprocedure", nullable = false, length = 255)
    private String nameProcedure;

    @Column(name = "procedurecode", nullable = false, length = 50, unique = true)
    private String procedureCode;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflowsidworkflow", nullable = false)
    @JsonIgnore
    private Workflows workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academiccalendaridacademiccalendar")
    @JsonIgnore
    private AcademicCalendar academicCalendar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deadlineruleid")
    @JsonIgnore
    private DeadLinerules deadLineRule;

    @Column(name = "estimateddurationdays")
    private Integer estimatedDurationDays;

    @Column(name = "requires2fa", nullable = false)
    private Boolean requires2fa = false;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}

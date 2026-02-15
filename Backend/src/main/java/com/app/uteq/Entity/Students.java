package com.app.uteq.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.TemporalType;

public class Students {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idstudent;

    @Column(nullable = false)
    private String semester;

    @Column(nullable = false, length = 1)
    private String parallel;

    @Column(name = "enrollment_number", nullable = false, length = 20)
    private String enrollmentNumber;

    @Column(name = "academic_period", length = 50)
    private String academicPeriod;

    @Temporal(TemporalType.DATE)
    private Date enrollmentdate;

    @Column(nullable = false)
    private String status = "active"; // active, inactive, graduated

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relación con el Usuario base
    @OneToOne
    @JoinColumn(name = "usersiduser", nullable = false)
    private User user;

    // Relación con la Carrera
    @ManyToOne
    @JoinColumn(name = "careersidcareer", nullable = false)
    private Career career;

}

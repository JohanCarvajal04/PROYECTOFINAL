package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "students")
@AllArgsConstructor
    @NoArgsConstructor
@Builder
public class Students {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idstudent")
    private Integer id;

    @Column(name = "semester", nullable = false, length = 255)
    private String semester;

    @Column(name = "parallel", nullable = false, length = 1)
    private String parallel;

    @ManyToOne
    @JoinColumn(name = "usersiduser", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "careersidcareer", nullable = false)
    private Careers career;

    @Column(name = "enrollmentdate")
    private LocalDate enrollmentDate;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "active";
}
package com.app.uteq.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "careers")
@AllArgsConstructor
@NoArgsConstructor

public class Careers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcareer")
    private Integer idCareer;

    @Column(name = "careername", nullable = false, length = 255)
    private String careerName;

    @Column(name = "careercode", length = 50)
    private String careerCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facultiesidfaculty", nullable = false)
    @JsonIgnore
    private Faculties faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinatoriduser")
    @JsonIgnore
    private Users coordinator;

}

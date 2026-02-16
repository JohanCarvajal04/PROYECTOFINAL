package com.app.uteq.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "careers")
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

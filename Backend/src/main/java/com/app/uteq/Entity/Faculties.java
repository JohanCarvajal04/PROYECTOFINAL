package com.app.uteq.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "faculties")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Faculties {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idfaculty")
    private Integer idFaculty;

    @Column(name = "facultyname", nullable = false, length = 255)
    private String facultyName;

    @Column(name = "facultycode", length = 50)
    private String facultyCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deaniduser")
    @JsonIgnore
    private Users dean;

}

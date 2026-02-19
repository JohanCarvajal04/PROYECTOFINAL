package com.app.uteq.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
    @JsonIgnore
    private Users dean;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

}

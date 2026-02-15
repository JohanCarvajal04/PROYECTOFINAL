package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "requirementsoftheprocedure")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequirementsOfTheProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrequirementsoftheprocedure")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "proceduresidprocedure", nullable = false)
    private Procedures procedure;

    @Column(name = "requirementname", nullable = false, length = 255)
    private String requirementName;

    @Column(name = "requirementdescription", columnDefinition = "TEXT")
    private String requirementDescription;

    @Column(name = "requirementtype", nullable = false, length = 50)
    private String requirementType;

    @Column(name = "ismandatory", nullable = false)
    private Boolean isMandatory = true;

    @Column(name = "displayorder")
    private Integer displayOrder;
}

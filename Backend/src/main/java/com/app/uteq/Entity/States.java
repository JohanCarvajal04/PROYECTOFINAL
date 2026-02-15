package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class States {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idstate")
    private Integer idState;

    @Column(name = "statename", nullable = false, length = 100, unique = true)
    private String stateName;

    @Column(name = "statedescription")
    private String stateDescription;

    @Column(name = "statecategory", nullable = false, length = 50)
    private String stateCategory;
}

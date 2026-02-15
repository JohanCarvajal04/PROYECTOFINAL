package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "deadlinerules")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeadLinerules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddeadlinerule")
    private Integer idDeadLineRule;

    @Column(name = "rulename", nullable = false, length = 255)
    private String ruleName;

    @Column(name = "procedurecategory", nullable = false, length = 100)
    private String procedureCategory;

    @Column(name = "basedeadlinedays", nullable = false)
    private Integer baseDeadlineDays;

    @Column(name = "warningdaysbefore", nullable = false)
    @Builder.Default
    private Integer warningDaysBefore = 3;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}

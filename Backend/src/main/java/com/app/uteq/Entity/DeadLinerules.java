package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
    private Integer warningDaysBefore = 3;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}

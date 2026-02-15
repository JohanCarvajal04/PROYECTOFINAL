package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "stagetracking")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idstagetracking")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "stateidstate", nullable = false)
    private States state;

    @ManyToOne
    @JoinColumn(name = "processingstageidprocessingstage", nullable = false)
    private ProcessingStage processingStage;

    @Column(name = "enteredat", nullable = false)
    private LocalDateTime enteredAt;

    @Column(name = "completedat")
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "assignedtouserid")
    private Users assignedToUser;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "applicationstagehistory")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationStageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idhistory")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "applicationidapplication", nullable = false)
    private Applications application;

    @ManyToOne
    @JoinColumn(name = "stagetrackingid", nullable = false)
    private StageTracking stageTracking;

    @Column(name = "enteredat", nullable = false)
    private LocalDateTime enteredAt;

    @Column(name = "exitedat")
    private LocalDateTime exitedAt;

    @ManyToOne
    @JoinColumn(name = "processedbyuserid")
    private Users processedByUser;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}

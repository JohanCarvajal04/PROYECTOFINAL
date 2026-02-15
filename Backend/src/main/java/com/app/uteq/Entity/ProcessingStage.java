package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processingstage")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessingStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idprocessingstage")
    private Integer idProcessingStage;

    @Column(name = "stagename", nullable = false, length = 255)
    private String stageName;

    @Column(name = "stagecode", nullable = false, length = 50, unique = true)
    private String stageCode;

    @Column(name = "stagedescription")
    private String stageDescription;

    @Column(name = "stageorder", nullable = false)
    private Integer stageOrder;

    @Column(name = "requiresapproval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    @Column(name = "maxdurationdays")
    private Integer maxDurationDays;
}

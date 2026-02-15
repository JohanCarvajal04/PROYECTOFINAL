package com.app.uteq.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workflowstages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkflowStages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idworkflowstage")
    private Integer idWorkflowStage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflowidworkflow", nullable = false)
    @JsonIgnore
    private Workflows workflow;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processingstageidprocessingstage", nullable = false)
    @JsonIgnore
    private ProcessingStage processingStage;

    @Column(name = "sequenceorder", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "isoptional", nullable = false)
    @Builder.Default
    private Boolean isOptional = false;
}

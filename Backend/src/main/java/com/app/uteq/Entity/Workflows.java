package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

 @Entity
 @Table(name = "workflows")
 @Data
 @AllArgsConstructor
 @NoArgsConstructor
 @Builder
public class Workflows {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idworkflow")
    private Integer idWorkflow;

    @Column(name = "workflowname", nullable = false, length = 255)
    private String workflowName;

    @Column(name = "workflowdescription")
    private String workflowDescription;

    @Column(name = "createdat")
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}

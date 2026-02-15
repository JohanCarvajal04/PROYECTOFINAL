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

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}

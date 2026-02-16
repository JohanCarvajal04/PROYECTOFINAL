package com.app.uteq.Controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.CWorkflowStageRequest;
import com.app.uteq.Dtos.UWorkflowStageRequest;
import com.app.uteq.Dtos.WorkflowStageResponse;
import com.app.uteq.Entity.ProcessingStage;
import com.app.uteq.Entity.WorkflowStages;
import com.app.uteq.Entity.Workflows;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.IWorkflowStagesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workflow-stages")
@PreAuthorize("isAuthenticated()")
public class WorkflowStagesController {
    private final IWorkflowStagesService service;

    @GetMapping
    @PreAuthorize("hasAuthority('FLUJOETAPA_LISTAR')")
    public ResponseEntity<List<WorkflowStageResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FLUJOETAPA_VER')")
    public ResponseEntity<WorkflowStageResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FLUJOETAPA_CREAR')")
    public ResponseEntity<WorkflowStageResponse> create(@Valid @RequestBody CWorkflowStageRequest request) {
        WorkflowStages entity = WorkflowStages.builder()
                .workflow(Workflows.builder().idWorkflow(request.getWorkflowIdWorkflow()).build())
                .processingStage(ProcessingStage.builder().idProcessingStage(request.getProcessingStageIdProcessingStage()).build())
                .sequenceOrder(request.getSequenceOrder())
                .isOptional(request.getIsOptional())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FLUJOETAPA_MODIFICAR')")
    public ResponseEntity<WorkflowStageResponse> update(@PathVariable Integer id, @Valid @RequestBody UWorkflowStageRequest request) {
        WorkflowStages entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStages", "id", id));
        entity.setWorkflow(Workflows.builder().idWorkflow(request.getWorkflowIdWorkflow()).build());
        entity.setProcessingStage(ProcessingStage.builder().idProcessingStage(request.getProcessingStageIdProcessingStage()).build());
        entity.setSequenceOrder(request.getSequenceOrder());
        entity.setIsOptional(request.getIsOptional());
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FLUJOETAPA_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private WorkflowStageResponse toResponse(WorkflowStages entity) {
        return new WorkflowStageResponse(
                entity.getIdWorkflowStage(),
                entity.getWorkflow() != null ? entity.getWorkflow().getIdWorkflow() : null,
                entity.getProcessingStage() != null ? entity.getProcessingStage().getIdProcessingStage() : null,
                entity.getSequenceOrder(),
                entity.getIsOptional()
        );
    }
}

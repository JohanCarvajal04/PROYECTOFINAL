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

import com.app.uteq.Dtos.CStageTrackingRequest;
import com.app.uteq.Dtos.StageTrackingResponse;
import com.app.uteq.Dtos.UStageTrackingRequest;
import com.app.uteq.Entity.ProcessingStage;
import com.app.uteq.Entity.StageTracking;
import com.app.uteq.Entity.States;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.IStageTrackingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stage-tracking")
@PreAuthorize("isAuthenticated()")
public class StageTrackingController {
    private final IStageTrackingService service;

    @GetMapping
    @PreAuthorize("hasAuthority('SEGUIMIENTO_LISTAR')")
    public ResponseEntity<List<StageTrackingResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGUIMIENTO_VER')")
    public ResponseEntity<StageTrackingResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SEGUIMIENTO_CREAR')")
    public ResponseEntity<StageTrackingResponse> create(@Valid @RequestBody CStageTrackingRequest request) {
        StageTracking entity = StageTracking.builder()
                .state(States.builder().idState(request.getStateIdState()).build())
                .processingStage(ProcessingStage.builder().idProcessingStage(request.getProcessingStageIdProcessingStage()).build())
                .assignedToUser(request.getAssignedToUserId() != null ? Users.builder().idUser(request.getAssignedToUserId()).build() : null)
                .notes(request.getNotes())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGUIMIENTO_MODIFICAR')")
    public ResponseEntity<StageTrackingResponse> update(@PathVariable Integer id, @Valid @RequestBody UStageTrackingRequest request) {
        StageTracking entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StageTracking", "id", id));
        entity.setState(States.builder().idState(request.getStateIdState()).build());
        entity.setProcessingStage(ProcessingStage.builder().idProcessingStage(request.getProcessingStageIdProcessingStage()).build());
        entity.setAssignedToUser(request.getAssignedToUserId() != null ? Users.builder().idUser(request.getAssignedToUserId()).build() : null);
        entity.setNotes(request.getNotes());
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGUIMIENTO_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private StageTrackingResponse toResponse(StageTracking entity) {
        return new StageTrackingResponse(
                entity.getId(),
                entity.getState() != null ? entity.getState().getIdState() : null,
                entity.getProcessingStage() != null ? entity.getProcessingStage().getIdProcessingStage() : null,
                entity.getEnteredAt(),
                entity.getCompletedAt(),
                entity.getAssignedToUser() != null ? entity.getAssignedToUser().getIdUser() : null,
                entity.getNotes()
        );
    }
}

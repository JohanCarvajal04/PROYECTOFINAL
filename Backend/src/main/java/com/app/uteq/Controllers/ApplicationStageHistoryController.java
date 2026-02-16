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

import com.app.uteq.Dtos.ApplicationStageHistoryResponse;
import com.app.uteq.Dtos.CApplicationStageHistoryRequest;
import com.app.uteq.Dtos.UApplicationStageHistoryRequest;
import com.app.uteq.Entity.ApplicationStageHistory;
import com.app.uteq.Entity.Applications;
import com.app.uteq.Entity.StageTracking;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.IApplicationStageHistoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/application-stage-history")
@PreAuthorize("isAuthenticated()")
public class ApplicationStageHistoryController {
    private final IApplicationStageHistoryService service;

    @GetMapping
    @PreAuthorize("hasAuthority('HIST_LISTAR')")
    public ResponseEntity<List<ApplicationStageHistoryResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HIST_VER')")
    public ResponseEntity<ApplicationStageHistoryResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HIST_CREAR')")
    public ResponseEntity<ApplicationStageHistoryResponse> create(@Valid @RequestBody CApplicationStageHistoryRequest request) {
        ApplicationStageHistory entity = ApplicationStageHistory.builder()
                .application(Applications.builder().id(request.getApplicationIdApplication()).build())
                .stageTracking(StageTracking.builder().id(request.getStageTrackingId()).build())
                .processedByUser(request.getProcessedByUserId() != null ? Users.builder().idUser(request.getProcessedByUserId()).build() : null)
                .comments(request.getComments())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HIST_MODIFICAR')")
    public ResponseEntity<ApplicationStageHistoryResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UApplicationStageHistoryRequest request) {
        ApplicationStageHistory entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationStageHistory", "id", id));
        entity.setApplication(Applications.builder().id(request.getApplicationIdApplication()).build());
        entity.setStageTracking(StageTracking.builder().id(request.getStageTrackingId()).build());
        entity.setProcessedByUser(request.getProcessedByUserId() != null ? Users.builder().idUser(request.getProcessedByUserId()).build() : null);
        entity.setComments(request.getComments());
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HIST_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ApplicationStageHistoryResponse toResponse(ApplicationStageHistory entity) {
        return new ApplicationStageHistoryResponse(
                entity.getId(),
                entity.getApplication() != null ? entity.getApplication().getId() : null,
                entity.getStageTracking() != null ? entity.getStageTracking().getId() : null,
                entity.getEnteredAt(),
                entity.getExitedAt(),
                entity.getProcessedByUser() != null ? entity.getProcessedByUser().getIdUser() : null,
                entity.getComments()
        );
    }
}

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

import com.app.uteq.Dtos.CNotificationTypeRequest;
import com.app.uteq.Dtos.NotificationTypeResponse;
import com.app.uteq.Dtos.UNotificationTypeRequest;
import com.app.uteq.Entity.NotificationType;
import com.app.uteq.Services.INotificationTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification-types")
@PreAuthorize("isAuthenticated()")
public class NotificationTypeController {
    private final INotificationTypeService service;

    @GetMapping
    @PreAuthorize("hasAuthority('TIPNOTIF_LISTAR')")
    public ResponseEntity<List<NotificationTypeResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TIPNOTIF_VER')")
    public ResponseEntity<NotificationTypeResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TIPNOTIF_CREAR')")
    public ResponseEntity<NotificationTypeResponse> create(@Valid @RequestBody CNotificationTypeRequest request) {
        NotificationType entity = NotificationType.builder()
                .nameTypeNotification(request.getNameTypeNotification())
                .templateCode(request.getTemplateCode())
                .priorityLevel(request.getPriorityLevel())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TIPNOTIF_MODIFICAR')")
    public ResponseEntity<NotificationTypeResponse> update(@PathVariable Integer id, @Valid @RequestBody UNotificationTypeRequest request) {
        NotificationType entity = NotificationType.builder()
                .idNotificationType(id)
                .nameTypeNotification(request.getNameTypeNotification())
                .templateCode(request.getTemplateCode())
                .priorityLevel(request.getPriorityLevel())
                .build();
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TIPNOTIF_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private NotificationTypeResponse toResponse(NotificationType entity) {
        return new NotificationTypeResponse(
                entity.getIdNotificationType(),
                entity.getNameTypeNotification(),
                entity.getTemplateCode(),
                entity.getPriorityLevel()
        );
    }
}

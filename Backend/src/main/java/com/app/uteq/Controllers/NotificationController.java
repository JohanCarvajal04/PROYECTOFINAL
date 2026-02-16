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

import com.app.uteq.Dtos.CNotificationRequest;
import com.app.uteq.Dtos.NotificationResponse;
import com.app.uteq.Dtos.UNotificationRequest;
import com.app.uteq.Entity.Applications;
import com.app.uteq.Entity.Notification;
import com.app.uteq.Entity.NotificationType;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.INotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {
    private final INotificationService service;

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIF_LISTAR')")
    public ResponseEntity<List<NotificationResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIF_VER')")
    public ResponseEntity<NotificationResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIF_CREAR')")
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody CNotificationRequest request) {
        Notification entity = Notification.builder()
                .notificationName(request.getNotificationName())
                .message(request.getMessage())
                .notificationType(NotificationType.builder().idNotificationType(request.getNotificationTypeIdNotificationType()).build())
                .application(request.getApplicationId() != null ? Applications.builder().id(request.getApplicationId()).build() : null)
                .recipientUser(Users.builder().idUser(request.getRecipientUserId()).build())
                .deliveryChannel(request.getDeliveryChannel())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIF_MODIFICAR')")
    public ResponseEntity<NotificationResponse> update(@PathVariable Integer id, @Valid @RequestBody UNotificationRequest request) {
        Notification entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        entity.setNotificationName(request.getNotificationName());
        entity.setMessage(request.getMessage());
        entity.setNotificationType(NotificationType.builder().idNotificationType(request.getNotificationTypeIdNotificationType()).build());
        entity.setApplication(request.getApplicationId() != null ? Applications.builder().id(request.getApplicationId()).build() : null);
        entity.setRecipientUser(Users.builder().idUser(request.getRecipientUserId()).build());
        entity.setDeliveryStatus(request.getDeliveryStatus());
        entity.setDeliveryChannel(request.getDeliveryChannel());
        entity.setRetryCount(request.getRetryCount());
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIF_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private NotificationResponse toResponse(Notification entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getNotificationName(),
                entity.getMessage(),
                entity.getNotificationType() != null ? entity.getNotificationType().getIdNotificationType() : null,
                entity.getApplication() != null ? entity.getApplication().getId() : null,
                entity.getRecipientUser() != null ? entity.getRecipientUser().getIdUser() : null,
                entity.getSentAt(),
                entity.getDeliveryStatus(),
                entity.getDeliveryChannel(),
                entity.getReadAt(),
                entity.getErrorMessage(),
                entity.getRetryCount()
        );
    }
}

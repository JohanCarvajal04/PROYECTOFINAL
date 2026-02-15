package com.app.uteq.Controllers;

import com.app.uteq.Entity.NotificationType;
import com.app.uteq.Services.INotificationTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification-types")
@PreAuthorize("isAuthenticated()")
public class NotificationTypeController {
    private final INotificationTypeService service;

    @GetMapping
    public ResponseEntity<List<NotificationType>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationType> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NotificationType> create(@RequestBody NotificationType entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationType> update(@PathVariable Integer id, @RequestBody NotificationType entity) {
        entity.setIdNotificationType(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

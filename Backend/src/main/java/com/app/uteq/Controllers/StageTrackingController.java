package com.app.uteq.Controllers;

import java.util.List;

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

import com.app.uteq.Entity.StageTracking;
import com.app.uteq.Services.IStageTrackingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stage-tracking")
@PreAuthorize("isAuthenticated()")
public class StageTrackingController {
    private final IStageTrackingService service;

    @GetMapping
    @PreAuthorize("hasAuthority('SEGUIMIENTO_LISTAR')")
    public ResponseEntity<List<StageTracking>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGUIMIENTO_VER')")
    public ResponseEntity<StageTracking> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SEGUIMIENTO_CREAR')")
    public ResponseEntity<StageTracking> create(@RequestBody StageTracking entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGUIMIENTO_MODIFICAR')")
    public ResponseEntity<StageTracking> update(@PathVariable Integer id, @RequestBody StageTracking entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGUIMIENTO_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

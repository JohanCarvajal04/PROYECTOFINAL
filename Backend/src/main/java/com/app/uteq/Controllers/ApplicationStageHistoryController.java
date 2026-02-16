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

import com.app.uteq.Entity.ApplicationStageHistory;
import com.app.uteq.Services.IApplicationStageHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/application-stage-history")
@PreAuthorize("isAuthenticated()")
public class ApplicationStageHistoryController {
    private final IApplicationStageHistoryService service;

    @GetMapping
    @PreAuthorize("hasAuthority('HIST_LISTAR')")
    public ResponseEntity<List<ApplicationStageHistory>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HIST_VER')")
    public ResponseEntity<ApplicationStageHistory> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HIST_CREAR')")
    public ResponseEntity<ApplicationStageHistory> create(@RequestBody ApplicationStageHistory entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HIST_MODIFICAR')")
    public ResponseEntity<ApplicationStageHistory> update(@PathVariable Integer id,
            @RequestBody ApplicationStageHistory entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HIST_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

package com.app.uteq.Controllers;

import com.app.uteq.Entity.ApplicationStageHistory;
import com.app.uteq.Services.IApplicationStageHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/application-stage-history")
@PreAuthorize("isAuthenticated()")
public class ApplicationStageHistoryController {
    private final IApplicationStageHistoryService service;

    @GetMapping
    public ResponseEntity<List<ApplicationStageHistory>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationStageHistory> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApplicationStageHistory> create(@RequestBody ApplicationStageHistory entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationStageHistory> update(@PathVariable Integer id,
            @RequestBody ApplicationStageHistory entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

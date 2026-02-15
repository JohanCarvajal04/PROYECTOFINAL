package com.app.uteq.Controllers;

import com.app.uteq.Entity.Procedures;
import com.app.uteq.Services.IProceduresService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/procedures")
@PreAuthorize("isAuthenticated()")
public class ProceduresController {
    private final IProceduresService service;

    @GetMapping
    public ResponseEntity<List<Procedures>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Procedures> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Procedures> create(@RequestBody Procedures entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Procedures> update(@PathVariable Integer id, @RequestBody Procedures entity) {
        entity.setIdProcedure(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

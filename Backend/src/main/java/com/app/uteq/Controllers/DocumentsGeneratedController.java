package com.app.uteq.Controllers;

import com.app.uteq.Entity.DocumentsGenerated;
import com.app.uteq.Services.IDocumentsGeneratedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents-generated")
public class DocumentsGeneratedController {
    private final IDocumentsGeneratedService service;

    @GetMapping
    public ResponseEntity<List<DocumentsGenerated>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentsGenerated> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DocumentsGenerated> create(@RequestBody DocumentsGenerated entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentsGenerated> update(@PathVariable Integer id, @RequestBody DocumentsGenerated entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

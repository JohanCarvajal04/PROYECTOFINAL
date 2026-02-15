package com.app.uteq.Controllers;

import com.app.uteq.Entity.AttachedDocuments;
import com.app.uteq.Services.IAttachedDocumentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attached-documents")
@PreAuthorize("isAuthenticated()")
public class AttachedDocumentsController {
    private final IAttachedDocumentsService service;

    @GetMapping
    public ResponseEntity<List<AttachedDocuments>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttachedDocuments> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AttachedDocuments> create(@RequestBody AttachedDocuments entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttachedDocuments> update(@PathVariable Integer id, @RequestBody AttachedDocuments entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

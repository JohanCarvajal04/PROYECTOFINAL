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

import com.app.uteq.Entity.DocumentsGenerated;
import com.app.uteq.Services.IDocumentsGeneratedService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents-generated")
@PreAuthorize("isAuthenticated()")
public class DocumentsGeneratedController {
    private final IDocumentsGeneratedService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DOCGEN_LISTAR')")
    public ResponseEntity<List<DocumentsGenerated>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCGEN_VER')")
    public ResponseEntity<DocumentsGenerated> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOCGEN_CREAR')")
    public ResponseEntity<DocumentsGenerated> create(@RequestBody DocumentsGenerated entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCGEN_MODIFICAR')")
    public ResponseEntity<DocumentsGenerated> update(@PathVariable Integer id, @RequestBody DocumentsGenerated entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCGEN_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

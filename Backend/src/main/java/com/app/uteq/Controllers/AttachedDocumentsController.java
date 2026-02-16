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

import com.app.uteq.Entity.AttachedDocuments;
import com.app.uteq.Services.IAttachedDocumentsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attached-documents")
@PreAuthorize("isAuthenticated()")
public class AttachedDocumentsController {
    private final IAttachedDocumentsService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DOCADJ_LISTAR')")
    public ResponseEntity<List<AttachedDocuments>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCADJ_VER')")
    public ResponseEntity<AttachedDocuments> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOCADJ_CREAR')")
    public ResponseEntity<AttachedDocuments> create(@RequestBody AttachedDocuments entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCADJ_MODIFICAR')")
    public ResponseEntity<AttachedDocuments> update(@PathVariable Integer id, @RequestBody AttachedDocuments entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCADJ_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

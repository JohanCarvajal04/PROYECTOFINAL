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

import com.app.uteq.Entity.DigitalSignatures;
import com.app.uteq.Services.IDigitalSignaturesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/digital-signatures")
@PreAuthorize("isAuthenticated()")
public class DigitalSignaturesController {
    private final IDigitalSignaturesService service;

    @GetMapping
    @PreAuthorize("hasAuthority('FIRMA_LISTAR')")
    public ResponseEntity<List<DigitalSignatures>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FIRMA_VER')")
    public ResponseEntity<DigitalSignatures> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FIRMA_CREAR')")
    public ResponseEntity<DigitalSignatures> create(@RequestBody DigitalSignatures entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FIRMA_MODIFICAR')")
    public ResponseEntity<DigitalSignatures> update(@PathVariable Integer id, @RequestBody DigitalSignatures entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FIRMA_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

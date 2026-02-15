package com.app.uteq.Controllers;

import com.app.uteq.Entity.DigitalSignatures;
import com.app.uteq.Services.IDigitalSignaturesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/digital-signatures")
@PreAuthorize("isAuthenticated()")
public class DigitalSignaturesController {
    private final IDigitalSignaturesService service;

    @GetMapping
    public ResponseEntity<List<DigitalSignatures>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalSignatures> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DigitalSignatures> create(@RequestBody DigitalSignatures entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DigitalSignatures> update(@PathVariable Integer id, @RequestBody DigitalSignatures entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

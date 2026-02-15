package com.app.uteq.Controllers;

import com.app.uteq.Entity.RefreshToken;
import com.app.uteq.Services.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/refresh-tokens")
@PreAuthorize("isAuthenticated()")
public class RefreshTokenController {
    private final IRefreshTokenService service;

    @GetMapping
    public ResponseEntity<List<RefreshToken>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefreshToken> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RefreshToken> create(@RequestBody RefreshToken entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RefreshToken> update(@PathVariable Long id, @RequestBody RefreshToken entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

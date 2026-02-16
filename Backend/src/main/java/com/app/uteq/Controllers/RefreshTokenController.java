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

import com.app.uteq.Entity.RefreshToken;
import com.app.uteq.Services.IRefreshTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/refresh-tokens")
@PreAuthorize("isAuthenticated()")
public class RefreshTokenController {
    private final IRefreshTokenService service;

    @GetMapping
    @PreAuthorize("hasAuthority('TOKEN_LISTAR')")
    public ResponseEntity<List<RefreshToken>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TOKEN_VER')")
    public ResponseEntity<RefreshToken> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TOKEN_CREAR')")
    public ResponseEntity<RefreshToken> create(@RequestBody RefreshToken entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TOKEN_MODIFICAR')")
    public ResponseEntity<RefreshToken> update(@PathVariable Long id, @RequestBody RefreshToken entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TOKEN_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

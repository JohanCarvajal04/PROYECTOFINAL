package com.app.uteq.Controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.RefreshTokenResponse;
import com.app.uteq.Entity.RefreshToken;
import com.app.uteq.Services.IRefreshTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/refresh-tokens")
@PreAuthorize("isAuthenticated()")
public class RefreshTokenController {
    private final IRefreshTokenService service;

    /**
     * Lista todos los refresh tokens SIN exponer el valor del token.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('TOKEN_LISTAR')")
    public ResponseEntity<List<RefreshTokenResponse>> findAll() {
        List<RefreshTokenResponse> response = service.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta un refresh token por ID SIN exponer el valor del token.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TOKEN_VER')")
    public ResponseEntity<RefreshTokenResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Revoca (elimina) un refresh token.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TOKEN_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private RefreshTokenResponse toResponse(RefreshToken entity) {
        return new RefreshTokenResponse(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getIdUser() : null,
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevoked(),
                entity.getDeviceInfo()
        );
    }
}

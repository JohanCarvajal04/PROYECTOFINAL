package com.app.uteq.Controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.CCredentialRequest;
import com.app.uteq.Dtos.CredentialResponse;
import com.app.uteq.Entity.Credentials;
import com.app.uteq.Services.ICredentialsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/credentials")
@PreAuthorize("isAuthenticated()")
public class CredentialsController {
    
    private final ICredentialsService service;

    // ═════════════════════════════════════════════════════════════
    // ENDPOINTS LEGACY
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/legacy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Credentials>> findAllLegacy() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/legacy/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Credentials> findByIdLegacy(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ═════════════════════════════════════════════════════════════
    // NUEVOS ENDPOINTS CON DTOs Y VALIDACIÓN
    // ═════════════════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CredentialResponse>> findAll() {
        return ResponseEntity.ok(service.findAllCredentials());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CredentialResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findCredentialById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CredentialResponse> create(@Valid @RequestBody CCredentialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createCredential(request));
    }

    // ═════════════════════════════════════════════════════════════
    // GESTIÓN DE CONTRASEÑAS
    // ═════════════════════════════════════════════════════════════

    @PostMapping("/{id}/change-password")
    public ResponseEntity<CredentialResponse> changePassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> passwordRequest) {
        String currentPassword = passwordRequest.get("currentPassword");
        String newPassword = passwordRequest.get("newPassword");
        return ResponseEntity.ok(service.changePassword(id, currentPassword, newPassword));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Integer id) {
        String tempPassword = service.resetPassword(id);
        return ResponseEntity.ok(Map.of(
                "message", "Contraseña reiniciada exitosamente",
                "temporaryPassword", tempPassword
        ));
    }

    @GetMapping("/{id}/password-expired")
    public ResponseEntity<Map<String, Boolean>> isPasswordExpired(@PathVariable Integer id) {
        boolean expired = service.isPasswordExpired(id);
        return ResponseEntity.ok(Map.of("expired", expired));
    }

    // ═════════════════════════════════════════════════════════════
    // BLOQUEO DE CUENTAS
    // ═════════════════════════════════════════════════════════════

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CredentialResponse> lockAccount(@PathVariable Integer id) {
        return ResponseEntity.ok(service.lockAccount(id));
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CredentialResponse> unlockAccount(@PathVariable Integer id) {
        return ResponseEntity.ok(service.unlockAccount(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

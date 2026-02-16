package com.app.uteq.Controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.app.uteq.Dtos.CSessionTokenRequest;
import com.app.uteq.Dtos.SessionTokenResponse;
import com.app.uteq.Dtos.USessionTokenRequest;
import com.app.uteq.Entity.SessionToken;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.ISessionTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/session-tokens")
@PreAuthorize("isAuthenticated()")
public class SessionTokenController {
    private final ISessionTokenService service;

    @GetMapping
    @PreAuthorize("hasAuthority('SESION_LISTAR')")
    public ResponseEntity<List<SessionTokenResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SESION_VER')")
    public ResponseEntity<SessionTokenResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SESION_CREAR')")
    public ResponseEntity<SessionTokenResponse> create(@Valid @RequestBody CSessionTokenRequest request) {
        SessionToken entity = SessionToken.builder()
                .user(Users.builder().idUser(request.getUserId()).build())
                .token(request.getToken())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SESION_MODIFICAR')")
    public ResponseEntity<SessionTokenResponse> update(@PathVariable Integer id, @Valid @RequestBody USessionTokenRequest request) {
        SessionToken entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SessionToken", "id", id));
        entity.setUser(Users.builder().idUser(request.getUserId()).build());
        entity.setToken(request.getToken());
        entity.setIpAddress(request.getIpAddress());
        entity.setUserAgent(request.getUserAgent());
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SESION_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private SessionTokenResponse toResponse(SessionToken entity) {
        return new SessionTokenResponse(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getIdUser() : null,
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getLastActivity()
        );
    }
}

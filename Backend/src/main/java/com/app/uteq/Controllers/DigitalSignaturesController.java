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

import com.app.uteq.Dtos.CDigitalSignatureRequest;
import com.app.uteq.Dtos.DigitalSignatureResponse;
import com.app.uteq.Dtos.UDigitalSignatureRequest;
import com.app.uteq.Entity.DigitalSignatures;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.IDigitalSignaturesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/digital-signatures")
@PreAuthorize("isAuthenticated()")
public class DigitalSignaturesController {
    private final IDigitalSignaturesService service;

    @GetMapping
    @PreAuthorize("hasAuthority('FIRMA_LISTAR')")
    public ResponseEntity<List<DigitalSignatureResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FIRMA_VER')")
    public ResponseEntity<DigitalSignatureResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FIRMA_CREAR')")
    public ResponseEntity<DigitalSignatureResponse> create(@Valid @RequestBody CDigitalSignatureRequest request) {
        DigitalSignatures entity = DigitalSignatures.builder()
                .user(Users.builder().idUser(request.getUserIdUser()).build())
                .certificatePath(request.getCertificatePath())
                .certificateSerial(request.getCertificateSerial())
                .issuer(request.getIssuer())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .signatureAlgorithm(request.getSignatureAlgorithm())
                .active(request.getActive())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FIRMA_MODIFICAR')")
    public ResponseEntity<DigitalSignatureResponse> update(@PathVariable Integer id, @Valid @RequestBody UDigitalSignatureRequest request) {
        DigitalSignatures entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalSignatures", "id", id));
        entity.setUser(Users.builder().idUser(request.getUserIdUser()).build());
        entity.setCertificatePath(request.getCertificatePath());
        entity.setCertificateSerial(request.getCertificateSerial());
        entity.setIssuer(request.getIssuer());
        entity.setValidFrom(request.getValidFrom());
        entity.setValidUntil(request.getValidUntil());
        entity.setSignatureAlgorithm(request.getSignatureAlgorithm());
        entity.setActive(request.getActive());
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FIRMA_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private DigitalSignatureResponse toResponse(DigitalSignatures entity) {
        return new DigitalSignatureResponse(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getIdUser() : null,
                entity.getCertificatePath(),
                entity.getCertificateSerial(),
                entity.getIssuer(),
                entity.getValidFrom(),
                entity.getValidUntil(),
                entity.getSignatureAlgorithm(),
                entity.getActive(),
                entity.getCreatedAt()
        );
    }
}

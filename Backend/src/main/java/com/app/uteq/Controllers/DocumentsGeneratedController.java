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

import com.app.uteq.Dtos.CDocumentGeneratedRequest;
import com.app.uteq.Dtos.DocumentGeneratedResponse;
import com.app.uteq.Dtos.UDocumentGeneratedRequest;
import com.app.uteq.Entity.Applications;
import com.app.uteq.Entity.DigitalSignatures;
import com.app.uteq.Entity.DocumentTemplates;
import com.app.uteq.Entity.DocumentsGenerated;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.IDocumentsGeneratedService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents-generated")
@PreAuthorize("isAuthenticated()")
public class DocumentsGeneratedController {
    private final IDocumentsGeneratedService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DOCGEN_LISTAR')")
    public ResponseEntity<List<DocumentGeneratedResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCGEN_VER')")
    public ResponseEntity<DocumentGeneratedResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOCGEN_CREAR')")
    public ResponseEntity<DocumentGeneratedResponse> create(@Valid @RequestBody CDocumentGeneratedRequest request) {
        DocumentsGenerated entity = DocumentsGenerated.builder()
                .application(Applications.builder().id(request.getApplicationsIdApplication()).build())
                .template(request.getTemplateId() != null ? DocumentTemplates.builder().idTemplate(request.getTemplateId()).build() : null)
                .documentType(request.getDocumentType())
                .documentPath(request.getDocumentPath())
                .generatedByUser(Users.builder().idUser(request.getGeneratedByUserId()).build())
                .digitalSignature(request.getDigitalSignatureId() != null ? DigitalSignatures.builder().id(request.getDigitalSignatureId()).build() : null)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCGEN_MODIFICAR')")
    public ResponseEntity<DocumentGeneratedResponse> update(@PathVariable Integer id, @Valid @RequestBody UDocumentGeneratedRequest request) {
        DocumentsGenerated entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentsGenerated", "id", id));
        entity.setApplication(Applications.builder().id(request.getApplicationsIdApplication()).build());
        entity.setTemplate(request.getTemplateId() != null ? DocumentTemplates.builder().idTemplate(request.getTemplateId()).build() : null);
        entity.setDocumentType(request.getDocumentType());
        entity.setDocumentPath(request.getDocumentPath());
        entity.setGeneratedByUser(Users.builder().idUser(request.getGeneratedByUserId()).build());
        entity.setDigitalSignature(request.getDigitalSignatureId() != null ? DigitalSignatures.builder().id(request.getDigitalSignatureId()).build() : null);
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCGEN_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private DocumentGeneratedResponse toResponse(DocumentsGenerated entity) {
        return new DocumentGeneratedResponse(
                entity.getId(),
                entity.getApplication() != null ? entity.getApplication().getId() : null,
                entity.getTemplate() != null ? entity.getTemplate().getIdTemplate() : null,
                entity.getDocumentType(),
                entity.getDocumentPath(),
                entity.getGeneratedAt(),
                entity.getGeneratedByUser() != null ? entity.getGeneratedByUser().getIdUser() : null,
                entity.getDigitalSignature() != null ? entity.getDigitalSignature().getId() : null,
                entity.getSignatureTimestamp()
        );
    }
}

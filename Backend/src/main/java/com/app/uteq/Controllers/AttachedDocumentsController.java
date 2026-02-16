package com.app.uteq.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.uteq.Dtos.AttachedDocumentResponse;
import com.app.uteq.Dtos.UAttachedDocumentRequest;
import com.app.uteq.Entity.Applications;
import com.app.uteq.Entity.AttachedDocuments;
import com.app.uteq.Entity.RequirementsOfTheProcedure;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.IAttachedDocumentsService;
import com.app.uteq.Services.IDriveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attached-documents")
@PreAuthorize("isAuthenticated()")
public class AttachedDocumentsController {
    private final IAttachedDocumentsService service;
    private final IDriveService driveService;

    @GetMapping
    @PreAuthorize("hasAuthority('DOCADJ_LISTAR')")
    public ResponseEntity<List<AttachedDocumentResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCADJ_VER')")
    public ResponseEntity<AttachedDocumentResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Sube un archivo a Google Drive y registra el documento adjunto en la base de datos.
     * Recibe el archivo como multipart y los IDs de relación como parámetros.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCADJ_CREAR')")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("applicationId") Integer applicationId,
            @RequestParam("requirementId") Integer requirementId,
            @RequestParam("uploadedByUserId") Integer uploadedByUserId) {
        try {
            // 1. Subir archivo a Google Drive
            String driveFileId = driveService.uploadFile(file);

            // 2. Crear registro en la base de datos
            AttachedDocuments document = AttachedDocuments.builder()
                    .application(Applications.builder().id(applicationId).build())
                    .requirement(RequirementsOfTheProcedure.builder().id(requirementId).build())
                    .fileName(file.getOriginalFilename())
                    .filePath(driveFileId) // Se almacena el ID de Google Drive
                    .fileSizeBytes(file.getSize())
                    .mimeType(file.getContentType())
                    .uploadDate(LocalDateTime.now())
                    .uploadedByUser(Users.builder().idUser(uploadedByUserId).build())
                    .build();

            AttachedDocuments saved = service.save(document);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al subir archivo: " + e.getMessage()));
        }
    }

    /**
     * Descarga un archivo desde Google Drive usando el fileId almacenado en el registro.
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('DOCADJ_VER')")
    public ResponseEntity<?> downloadFile(@PathVariable Integer id) {
        return service.findById(id)
                .map(doc -> {
                    try {
                        byte[] content = driveService.downloadFile(doc.getFilePath());
                        return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                        "attachment; filename=\"" + doc.getFileName() + "\"")
                                .body(content);
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError()
                                .body(("Error al descargar archivo: " + e.getMessage()).getBytes());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCADJ_MODIFICAR')")
    public ResponseEntity<AttachedDocumentResponse> update(@PathVariable Integer id, @Valid @RequestBody UAttachedDocumentRequest request) {
        AttachedDocuments entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttachedDocuments", "id", id));
        entity.setApplication(Applications.builder().id(request.getApplicationsIdApplication()).build());
        entity.setRequirement(request.getRequirementId() != null ? RequirementsOfTheProcedure.builder().id(request.getRequirementId()).build() : null);
        entity.setFileName(request.getFileName());
        entity.setFilePath(request.getFilePath());
        entity.setFileSizeBytes(request.getFileSizeBytes());
        entity.setMimeType(request.getMimeType());
        entity.setUploadedByUser(Users.builder().idUser(request.getUploadedByUserId()).build());
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    /**
     * Elimina el documento adjunto de la base de datos y de Google Drive.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCADJ_ELIMINAR')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return service.findById(id)
                .map(doc -> {
                    try {
                        // 1. Eliminar de Google Drive
                        driveService.deleteFile(doc.getFilePath());
                    } catch (Exception e) {
                        // Log pero no fallar si Drive ya no tiene el archivo
                    }
                    // 2. Eliminar de la base de datos
                    service.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private AttachedDocumentResponse toResponse(AttachedDocuments entity) {
        return new AttachedDocumentResponse(
                entity.getId(),
                entity.getApplication() != null ? entity.getApplication().getId() : null,
                entity.getRequirement() != null ? entity.getRequirement().getId() : null,
                entity.getFileName(),
                entity.getFilePath(),
                entity.getFileSizeBytes(),
                entity.getMimeType(),
                entity.getUploadDate(),
                entity.getUploadedByUser() != null ? entity.getUploadedByUser().getIdUser() : null
        );
    }
}

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.CProcedureRequest;
import com.app.uteq.Dtos.ProcedureResponse;
import com.app.uteq.Dtos.UProcedureRequest;
import com.app.uteq.Services.IProceduresService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/procedures")
@PreAuthorize("isAuthenticated()")
public class ProceduresController {
    
    private final IProceduresService service;

    // ═════════════════════════════════════════════════════════════
    // ENDPOINTS CON DTOs Y VALIDACIÓN
    // ═════════════════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("hasAuthority('TRAMITE_LISTAR')")
    public ResponseEntity<List<ProcedureResponse>> findAll() {
        return ResponseEntity.ok(service.findAllProcedures());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('TRAMITE_LISTAR')")
    public ResponseEntity<List<ProcedureResponse>> findAllIncludingInactive() {
        return ResponseEntity.ok(service.findAllIncludingInactive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRAMITE_VER')")
    public ResponseEntity<ProcedureResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findProcedureById(id));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('TRAMITE_VER')")
    public ResponseEntity<ProcedureResponse> findByCode(@PathVariable String code) {
        return service.findByProcedureCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/workflow/{workflowId}")
    @PreAuthorize("hasAuthority('TRAMITE_LISTAR')")
    public ResponseEntity<List<ProcedureResponse>> findByWorkflow(@PathVariable Integer workflowId) {
        return ResponseEntity.ok(service.findByWorkflow(workflowId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRAMITE_CREAR')")
    public ResponseEntity<ProcedureResponse> create(@Valid @RequestBody CProcedureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createProcedure(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TRAMITE_MODIFICAR')")
    public ResponseEntity<ProcedureResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UProcedureRequest request) {
        return ResponseEntity.ok(service.updateProcedure(id, request));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('TRAMITE_ACTIVAR')")
    public ResponseEntity<ProcedureResponse> activate(@PathVariable Integer id) {
        return ResponseEntity.ok(service.activateProcedure(id));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('TRAMITE_DESACTIVAR')")
    public ResponseEntity<ProcedureResponse> deactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(service.deactivateProcedure(id));
    }

    @GetMapping("/{id}/requires-2fa")
    @PreAuthorize("hasAuthority('TRAMITE_VER')")
    public ResponseEntity<Map<String, Boolean>> requires2FA(@PathVariable Integer id) {
        boolean requires2fa = service.requires2FA(id);
        return ResponseEntity.ok(Map.of("requires2FA", requires2fa));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TRAMITE_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteProcedure(id);
        return ResponseEntity.noContent().build();
    }
}

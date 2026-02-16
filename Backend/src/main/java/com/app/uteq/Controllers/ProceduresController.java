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
import com.app.uteq.Entity.Procedures;
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
    // ENDPOINTS LEGACY
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/legacy")
    public ResponseEntity<List<Procedures>> findAllLegacy() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/legacy/{id}")
    public ResponseEntity<Procedures> findByIdLegacy(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ═════════════════════════════════════════════════════════════
    // NUEVOS ENDPOINTS CON DTOs Y VALIDACIÓN
    // ═════════════════════════════════════════════════════════════

    @GetMapping
    public ResponseEntity<List<ProcedureResponse>> findAll() {
        return ResponseEntity.ok(service.findAllProcedures());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProcedureResponse>> findAllIncludingInactive() {
        return ResponseEntity.ok(service.findAllIncludingInactive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcedureResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findProcedureById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ProcedureResponse> findByCode(@PathVariable String code) {
        return service.findByProcedureCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<List<ProcedureResponse>> findByWorkflow(@PathVariable Integer workflowId) {
        return ResponseEntity.ok(service.findByWorkflow(workflowId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcedureResponse> create(@Valid @RequestBody CProcedureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createProcedure(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcedureResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UProcedureRequest request) {
        return ResponseEntity.ok(service.updateProcedure(id, request));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcedureResponse> activate(@PathVariable Integer id) {
        return ResponseEntity.ok(service.activateProcedure(id));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcedureResponse> deactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(service.deactivateProcedure(id));
    }

    @GetMapping("/{id}/requires-2fa")
    public ResponseEntity<Map<String, Boolean>> requires2FA(@PathVariable Integer id) {
        boolean requires2fa = service.requires2FA(id);
        return ResponseEntity.ok(Map.of("requires2FA", requires2fa));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteProcedure(id);
        return ResponseEntity.noContent().build();
    }
}

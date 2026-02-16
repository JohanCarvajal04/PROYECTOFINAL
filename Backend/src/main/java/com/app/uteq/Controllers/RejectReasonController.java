package com.app.uteq.Controllers;

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

import com.app.uteq.Dtos.CRejectionReasonRequest;
import com.app.uteq.Dtos.URejectionReasonRequest;
import com.app.uteq.Services.IRejectionReasonsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reject-reason")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class RejectReasonController {
    private final IRejectionReasonsService service;

    // CREATE -> spi_rejectionreason
    @PostMapping
    @PreAuthorize("hasAuthority('RECHAZO_CREAR')")
    public ResponseEntity<?> create(@Valid @RequestBody CRejectionReasonRequest request) {
        service.createRejectreason(request);
        return ResponseEntity.ok("Razón de rechazo creada correctamente");
    }

    // UPDATE -> spu_rejectionreason
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RECHAZO_MODIFICAR')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody URejectionReasonRequest request) {
        request.setIdrejectionreason(id);
        service.updateRejectreason(request);
        return ResponseEntity.ok("Razón de rechazo actualizada correctamente");
    }

    // DELETE lógico -> spd_rejectionreason
    @DeleteMapping("/{idrejectionreason}")
    @PreAuthorize("hasAuthority('RECHAZO_ELIMINAR')")
    public ResponseEntity<?> delete(@PathVariable Integer idrejectionreason) {
        service.deleteRejectreason(idrejectionreason);
        return ResponseEntity.ok("Razón de rechazo eliminada lógicamente (active=false)");
    }

    // LIST -> fn_list_rejectionreasons
    @GetMapping
    @PreAuthorize("hasAuthority('RECHAZO_LISTAR')")
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listRejectreason(onlyActive));
    }
}

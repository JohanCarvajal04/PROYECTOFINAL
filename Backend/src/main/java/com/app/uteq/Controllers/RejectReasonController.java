package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CRejectionReasonRequest;
import com.app.uteq.Dtos.URejectionReasonRequest;
import com.app.uteq.Services.IRejectionReasonsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/reject-reason")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class RejectReasonController {
    private final IRejectionReasonsService service;

    // CREATE -> spi_rejectionreason
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CRejectionReasonRequest request) {
        service.createRejectreason(request);
        return ResponseEntity.ok("Razón de rechazo creada correctamente");
    }

    // UPDATE -> spu_rejectionreason
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody URejectionReasonRequest request) {
        request.setIdrejectionreason(id);
        service.updateRejectreason(request);
        return ResponseEntity.ok("Razón de rechazo actualizada correctamente");
    }

    // DELETE lógico -> spd_rejectionreason
    @DeleteMapping("/{idrejectionreason}")
    public ResponseEntity<?> delete(@PathVariable Integer idrejectionreason) {
        service.deleteRejectreason(idrejectionreason);
        return ResponseEntity.ok("Razón de rechazo eliminada lógicamente (active=false)");
    }

    // LIST -> fn_list_rejectionreasons
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listRejectreason(onlyActive));
    }
}

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

import com.app.uteq.Dtos.CCareersRequest;
import com.app.uteq.Dtos.UCareersRequest;
import com.app.uteq.Services.ICareersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/careers")
@PreAuthorize("isAuthenticated()")
public class CareerController {
    private final ICareersService service;

    // CREATE
    @PostMapping
    @PreAuthorize("hasAuthority('CARRERA_CREAR')")
    public ResponseEntity<?> create(@Valid @RequestBody CCareersRequest request) {
        service.createCareers(request);
        return ResponseEntity.ok("Carrera creada correctamente");
    }

    // UPDATE (reemplazo total)
    @PutMapping("/{idcareer}")
    @PreAuthorize("hasAuthority('CARRERA_MODIFICAR')")
    public ResponseEntity<?> update(
            @PathVariable Integer idcareer,
            @Valid @RequestBody UCareersRequest request) {

        request.setIdcareer(idcareer);
        service.updateCareers(request);
        return ResponseEntity.ok("Carrera actualizada correctamente");
    }

    // DELETE
    @DeleteMapping("/{idcareer}")
    @PreAuthorize("hasAuthority('CARRERA_ELIMINAR')")
    public ResponseEntity<?> delete(@PathVariable Integer idcareer) {
        service.deleteCareers(idcareer);
        return ResponseEntity.ok("Carrera eliminada correctamente");
    }

    // LIST (filtro opcional por facultad)
    @GetMapping
    @PreAuthorize("hasAuthority('CARRERA_LISTAR')")
    public ResponseEntity<?> list(@RequestParam(required = false) Integer facultyid) {
        return ResponseEntity.ok(service.listCareers(facultyid));
    }
}

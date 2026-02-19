package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CFacultyRequest;
import com.app.uteq.Dtos.UFacultyRequest;
import com.app.uteq.Services.IFacultiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/faculty")
@RequiredArgsConstructor
public class FacultyController {
    private final IFacultiesService service;

    // CREATE -> spi_faculty
    @PostMapping
    @PreAuthorize("hasAuthority('FACULTAD_CREAR')")
    public ResponseEntity<?> create(@RequestBody CFacultyRequest request) {
        service.createFaculty(request);
        return ResponseEntity.ok("Facultad creada correctamente");
    }

    // UPDATE -> spu_faculty
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FACULTAD_MODIFICAR')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody UFacultyRequest request) {
        request.setIdfaculty(id);
        service.updateFaculty(request);
        return ResponseEntity.ok("Facultad actualizada correctamente");
    }

    // DELETE -> spd_faculty
    @DeleteMapping("/{idfaculty}")
    @PreAuthorize("hasAuthority('FACULTAD_ELIMINAR')")
    public ResponseEntity<?> delete(@PathVariable Integer idfaculty) {
        service.deleteFaculty(idfaculty);
        return ResponseEntity.ok("Facultad eliminada correctamente");
    }

    // LIST -> fn_list_faculties
    @GetMapping
    @PreAuthorize("hasAuthority('FACULTAD_LISTAR')")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.listFaculty());
    }
}

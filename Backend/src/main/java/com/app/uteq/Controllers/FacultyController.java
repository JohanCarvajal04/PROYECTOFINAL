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
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.CFacultyRequest;
import com.app.uteq.Dtos.UFacultyRequest;
import com.app.uteq.Services.IFacultiesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/faculty")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FacultyController {
    private final IFacultiesService service;

    // CREATE -> spi_faculty
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CFacultyRequest request) {
        service.createFaculty(request);
        return ResponseEntity.ok("Facultad creada correctamente");
    }

    // UPDATE -> spu_faculty
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody UFacultyRequest request) {
        request.setIdfaculty(id);
        service.updateFaculty(request);
        return ResponseEntity.ok("Facultad actualizada correctamente");
    }

    // DELETE -> spd_faculty
    @DeleteMapping("/{idfaculty}")
    public ResponseEntity<?> delete(@PathVariable Integer idfaculty) {
        service.deleteFaculty(idfaculty);
        return ResponseEntity.ok("Facultad eliminada correctamente");
    }

    // LIST -> fn_list_faculties
    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.listFaculty());
    }
}

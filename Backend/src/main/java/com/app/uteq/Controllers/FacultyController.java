package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CFacultyRequest;
import com.app.uteq.Dtos.UFacultyRequest;
import com.app.uteq.Services.IFacultiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/faculty")
@RequiredArgsConstructor
public class FacultyController {
    private final IFacultiesService service;

    // CREATE -> spi_faculty
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CFacultyRequest request) {
        service.createFaculty(request);
        return ResponseEntity.ok("Facultad creada correctamente");
    }

    // UPDATE -> spu_faculty
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody UFacultyRequest request) {
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

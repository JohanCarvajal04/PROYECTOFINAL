package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CCareersRequest;
import com.app.uteq.Dtos.UCareersRequest;
import com.app.uteq.Services.ICareersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/careers")
public class CareerController {
    private final ICareersService service;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CCareersRequest request) {
        service.createCareers(request);
        return ResponseEntity.ok("Carrera creada correctamente");
    }

    // UPDATE (reemplazo total)
    @PutMapping("/{idcareer}")
    public ResponseEntity<?> update(
            @PathVariable Integer idcareer,
            @RequestBody UCareersRequest request) {

        request.setIdcareer(idcareer);
        service.updateCareers(request);
        return ResponseEntity.ok("Carrera actualizada correctamente");
    }

    // DELETE
    @DeleteMapping("/{idcareer}")
    public ResponseEntity<?> delete(@PathVariable Integer idcareer) {
        service.deleteCareers(idcareer);
        return ResponseEntity.ok("Carrera eliminada correctamente");
    }

    // LIST (filtro opcional por facultad)
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Integer facultyid) {
        return ResponseEntity.ok(service.listCareers(facultyid));
    }
}

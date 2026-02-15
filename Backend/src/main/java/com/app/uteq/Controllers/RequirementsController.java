package com.app.uteq.Controllers;

import com.app.uteq.Entity.RequirementsOfTheProcedure;
import com.app.uteq.Services.IRequirementsOfTheProcedureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/requirements")
@PreAuthorize("isAuthenticated()")
public class RequirementsController {
    private final IRequirementsOfTheProcedureService service;

    @GetMapping
    public ResponseEntity<List<RequirementsOfTheProcedure>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequirementsOfTheProcedure> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RequirementsOfTheProcedure> create(@RequestBody RequirementsOfTheProcedure entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequirementsOfTheProcedure> update(@PathVariable Integer id,
            @RequestBody RequirementsOfTheProcedure entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

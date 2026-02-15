package com.app.uteq.Controllers;

import com.app.uteq.Entity.Applications;
import com.app.uteq.Services.IApplicationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ApplicationsController {
    private final IApplicationsService service;

    @GetMapping
    public ResponseEntity<List<Applications>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Applications> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Applications> create(@RequestBody Applications applications) {
        return ResponseEntity.ok(service.save(applications));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Applications> update(@PathVariable Integer id, @RequestBody Applications applications) {
        applications.setId(id);
        return ResponseEntity.ok(service.save(applications));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

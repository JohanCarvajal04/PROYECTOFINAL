package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CDeadlineRuleRequest;
import com.app.uteq.Dtos.UDeadlineRuleRequest;
import com.app.uteq.Services.IDeadLineRulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/deadlinerules")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeadlineruleControllers {
    private final IDeadLineRulesService service;

    // CREATE -> spi_deadlinerule
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CDeadlineRuleRequest request) {
        service.createDeadlinerule(request);
        return ResponseEntity.ok("Regla creada correctamente");
    }

    // UPDATE -> spu_deadlinerule
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody UDeadlineRuleRequest request) {
        request.setIddeadlinerule(id);
        service.updateDeadlinerule(request);
        return ResponseEntity.ok("Regla actualizada correctamente");
    }

    // DELETE lógico -> spd_deadlinerule
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.deleteDeadlinerule(id);
        return ResponseEntity.ok("Regla eliminada correctamente");
    }

    // LIST -> fn_list_deadlinerules
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listDeadlinerule(onlyActive));
    }
}

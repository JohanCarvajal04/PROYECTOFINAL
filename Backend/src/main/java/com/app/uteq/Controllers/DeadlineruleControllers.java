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

import com.app.uteq.Dtos.CDeadlineRuleRequest;
import com.app.uteq.Dtos.UDeadlineRuleRequest;
import com.app.uteq.Services.IDeadLineRulesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/deadlinerules")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeadlineruleControllers {
    private final IDeadLineRulesService service;

    // CREATE -> spi_deadlinerule
    @PostMapping
    @PreAuthorize("hasAuthority('REGLA_CREAR')")
    public ResponseEntity<?> create(@Valid @RequestBody CDeadlineRuleRequest request) {
        service.createDeadlinerule(request);
        return ResponseEntity.ok("Regla creada correctamente");
    }

    // UPDATE -> spu_deadlinerule
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REGLA_MODIFICAR')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody UDeadlineRuleRequest request) {
        request.setIddeadlinerule(id);
        service.updateDeadlinerule(request);
        return ResponseEntity.ok("Regla actualizada correctamente");
    }

    // DELETE lógico -> spd_deadlinerule
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REGLA_ELIMINAR')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.deleteDeadlinerule(id);
        return ResponseEntity.ok("Regla eliminada correctamente");
    }

    // LIST -> fn_list_deadlinerules
    @GetMapping
    @PreAuthorize("hasAuthority('REGLA_LISTAR')")
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listDeadlinerule(onlyActive));
    }
}

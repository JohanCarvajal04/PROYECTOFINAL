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

import com.app.uteq.Dtos.CWorkflowRequest;
import com.app.uteq.Dtos.UWorkflowRequest;
import com.app.uteq.Services.IWorkflowsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/work-flows")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WorkFlowsController {
    private final IWorkflowsService service;

    // CREATE -> spi_workflow
    @PostMapping
    @PreAuthorize("hasAuthority('FLUJO_CREAR')")
    public ResponseEntity<?> create(@Valid @RequestBody CWorkflowRequest request) {
        service.createWorkflow(request);
        return ResponseEntity.ok("Workflow creado correctamente");
    }

    // UPDATE -> spu_workflow
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FLUJO_MODIFICAR')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody UWorkflowRequest request) {
        request.setIdworkflow(id);
        service.updateWorkflow(request);
        return ResponseEntity.ok("Workflow actualizado correctamente");
    }

    // DELETE lógico -> spd_workflow
    @DeleteMapping("/{idworkflow}")
    @PreAuthorize("hasAuthority('FLUJO_ELIMINAR')")
    public ResponseEntity<?> delete(@PathVariable Integer idworkflow) {
        service.deleteWorkflow(idworkflow);
        return ResponseEntity.ok("Workflow desactivado correctamente (active=false)");
    }

    // LIST -> fn_list_workflows
    @GetMapping
    @PreAuthorize("hasAuthority('FLUJO_LISTAR')")
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listWorkflow(onlyActive));
    }
}

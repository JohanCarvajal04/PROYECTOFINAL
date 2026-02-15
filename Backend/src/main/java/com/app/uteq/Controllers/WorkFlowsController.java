package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CWorkflowRequest;
import com.app.uteq.Dtos.UWorkflowRequest;
import com.app.uteq.Services.IWorkflowsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/work-flows")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WorkFlowsController {
    private final IWorkflowsService service;

    // CREATE -> spi_workflow
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CWorkflowRequest request) {
        service.createWorkflow(request);
        return ResponseEntity.ok("Workflow creado correctamente");
    }

    // UPDATE -> spu_workflow
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody UWorkflowRequest request) {
        request.setIdworkflow(id);
        service.updateWorkflow(request);
        return ResponseEntity.ok("Workflow actualizado correctamente");
    }

    // DELETE lógico -> spd_workflow
    @DeleteMapping("/{idworkflow}")
    public ResponseEntity<?> delete(@PathVariable Integer idworkflow) {
        service.deleteWorkflow(idworkflow);
        return ResponseEntity.ok("Workflow desactivado correctamente (active=false)");
    }

    // LIST -> fn_list_workflows
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listWorkflow(onlyActive));
    }
}

package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CWorkflowRequest;
import com.app.uteq.Dtos.UWorkflowRequest;
import com.app.uteq.Services.IWorkflowsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/work-flows")
@RequiredArgsConstructor
public class WorkFlowsController {
    private final IWorkflowsService service;

    // CREATE -> spi_workflow
    @PostMapping("/sp-create")
    public ResponseEntity<?> create(@RequestBody CWorkflowRequest request) {
        service.createWorkflow(request);
        return ResponseEntity.ok("Workflow creado correctamente");
    }

    // UPDATE -> spu_workflow
    @PutMapping("/sp-update")
    public ResponseEntity<?> update(@RequestBody UWorkflowRequest request) {
        service.updateWorkflow(request);
        return ResponseEntity.ok("Workflow actualizado correctamente");
    }

    // DELETE lógico -> spd_workflow
    @DeleteMapping("/sp-delete/{idworkflow}")
    public ResponseEntity<?> delete(@PathVariable Integer idworkflow) {
        service.deleteWorkflow(idworkflow);
        return ResponseEntity.ok("Workflow desactivado correctamente (active=false)");
    }

    // LIST -> fn_list_workflows
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listWorkflow(onlyActive));
    }
}

package com.app.uteq.Controllers;

import com.app.uteq.Entity.WorkflowStages;
import com.app.uteq.Services.IWorkflowStagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workflow-stages")
public class WorkflowStagesController {
    private final IWorkflowStagesService service;

    @GetMapping
    public ResponseEntity<List<WorkflowStages>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowStages> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkflowStages> create(@RequestBody WorkflowStages entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkflowStages> update(@PathVariable Integer id, @RequestBody WorkflowStages entity) {
        entity.setIdWorkflowStage(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

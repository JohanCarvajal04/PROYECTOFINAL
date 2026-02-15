package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CDeadlineRuleRequest;
import com.app.uteq.Dtos.UDeadlineRuleRequest;
import com.app.uteq.Services.IDeadLineRulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dlr")
@RequiredArgsConstructor
public class DeadlineruleControllers {
    private final IDeadLineRulesService service;

    // CREATE -> spi_deadlinerule
    @PostMapping("/sp-create")
    public ResponseEntity<?> create(@RequestBody CDeadlineRuleRequest request) {
        service.createDeadlinerule(request);
        return ResponseEntity.ok("Regla creada correctamente");
    }

    // UPDATE -> spu_deadlinerule
    @PutMapping("/sp-update")
    public ResponseEntity<?> update(@RequestBody UDeadlineRuleRequest request) {
        service.updateDeadlinerule(request);
        return ResponseEntity.ok("Regla actualizada correctamente");
    }

    // DELETE lógico -> spd_deadlinerule
    @DeleteMapping("/sp-delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.deleteDeadlinerule(id);
        return ResponseEntity.ok("Regla eliminada correctamente");
    }

    // LIST -> fn_list_deadlinerules
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.listDeadlinerule(onlyActive));
    }
}

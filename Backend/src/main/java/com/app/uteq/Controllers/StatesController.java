package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CStateRequest;
import com.app.uteq.Dtos.UStateRequest;
import com.app.uteq.Services.IStatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/states")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class StatesController {
    private final IStatesService service;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CStateRequest request) {
        service.createStates(request);
        return ResponseEntity.ok("Estado creado correctamente");
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody UStateRequest request) {
        request.setIdstate(id);
        service.updateStates(request);
        return ResponseEntity.ok("Estado actualizado correctamente");
    }

    // DELETE
    @DeleteMapping("/{idstate}")
    public ResponseEntity<?> delete(@PathVariable Integer idstate) {
        service.deleteStates(idstate);
        return ResponseEntity.ok("Estado eliminado correctamente");
    }

    // LIST
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(service.listStates(category));
    }
}

package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CStateRequest;
import com.app.uteq.Dtos.UStateRequest;
import com.app.uteq.Services.IStatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
public class StatesController {
    private final IStatesService service;

    // CREATE
    @PostMapping("/sp-create")
    public ResponseEntity<?> create(@RequestBody CStateRequest request) {
        service.createStates(request);
        return ResponseEntity.ok("Estado creado correctamente");
    }

    // UPDATE
    @PutMapping("/sp-update")
    public ResponseEntity<?> update(@RequestBody UStateRequest request) {
        service.updateStates(request);
        return ResponseEntity.ok("Estado actualizado correctamente");
    }

    // DELETE
    @DeleteMapping("/sp-delete/{idstate}")
    public ResponseEntity<?> delete(@PathVariable Integer idstate) {
        service.deleteStates(idstate);
        return ResponseEntity.ok("Estado eliminado correctamente");
    }

    // LIST
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(service.listStates(category));
    }
}

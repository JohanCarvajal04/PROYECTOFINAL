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
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.CProcessingStageRequest;
import com.app.uteq.Dtos.UProcessingStageRequest;
import com.app.uteq.Services.IProcessingStageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/processing-stages")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProcessingStageController {
    private final IProcessingStageService service;

    // CREATE -> spi_processingstage
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CProcessingStageRequest request) {
        service.createProcessingstage(request);
        return ResponseEntity.ok("ProcessingStage creado correctamente");
    }

    // UPDATE -> spu_processingstage
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody UProcessingStageRequest request) {
        request.setIdprocessingstage(id);
        service.updateProcessingstage(request);
        return ResponseEntity.ok("ProcessingStage actualizado correctamente");
    }

    // DELETE -> spd_processingstage
    @DeleteMapping("/{idprocessingstage}")
    public ResponseEntity<?> delete(@PathVariable Integer idprocessingstage) {
        service.deleteProcessingstage(idprocessingstage);
        return ResponseEntity.ok("ProcessingStage eliminado correctamente");
    }

    // LIST -> fn_list_processingstage
    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.listProcessingstage());
    }
}

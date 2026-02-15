package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CProcessingStageRequest;
import com.app.uteq.Dtos.UProcessingStageRequest;
import com.app.uteq.Services.IProcessingStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/processing-stages")
@RequiredArgsConstructor
public class ProcessingStageController {
    private final IProcessingStageService service;

    // CREATE -> spi_processingstage
    @PostMapping("/sp-create")
    public ResponseEntity<?> create(@RequestBody CProcessingStageRequest request) {
        service.createProcessingstage(request);
        return ResponseEntity.ok("ProcessingStage creado correctamente");
    }

    // UPDATE -> spu_processingstage
    @PutMapping("/sp-update")
    public ResponseEntity<?> update(@RequestBody UProcessingStageRequest request) {
        service.updateProcessingstage(request);
        return ResponseEntity.ok("ProcessingStage actualizado correctamente");
    }

    // DELETE -> spd_processingstage
    @DeleteMapping("/sp-delete/{idprocessingstage}")
    public ResponseEntity<?> delete(@PathVariable Integer idprocessingstage) {
        service.deleteProcessingstage(idprocessingstage);
        return ResponseEntity.ok("ProcessingStage eliminado correctamente");
    }

    // LIST -> fn_list_processingstage
    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.listProcessingstage());
    }
}

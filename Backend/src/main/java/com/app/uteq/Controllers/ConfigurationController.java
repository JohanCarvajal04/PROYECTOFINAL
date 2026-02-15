package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CConfigurationRequest;
import com.app.uteq.Dtos.UConfigurationRequest;
import com.app.uteq.Services.IConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuration")
@RequiredArgsConstructor
public class ConfigurationController {
    private final IConfigurationService service;

    @PostMapping("/sp-create")
    public ResponseEntity<?> create(@RequestBody CConfigurationRequest request) {
        service.createConfiguration(request);
        return ResponseEntity.ok("Configuración creada correctamente");
    }

    @PutMapping("/sp-update")
    public ResponseEntity<?> update(@RequestBody UConfigurationRequest request) {
        service.updateConfiguration(request);
        return ResponseEntity.ok("Configuración actualizada correctamente");
    }

    @DeleteMapping("/sp-delete/{idconfiguration}")
    public ResponseEntity<?> delete(@PathVariable Integer idconfiguration) {
        service.deleteConfiguration(idconfiguration);
        return ResponseEntity.ok("Configuración eliminada correctamente");
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.listConfiguration());
    }
}

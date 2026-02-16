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

import com.app.uteq.Dtos.CConfigurationRequest;
import com.app.uteq.Dtos.UConfigurationRequest;
import com.app.uteq.Services.IConfigurationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/configuration")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
public class ConfigurationController {
    private final IConfigurationService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CConfigurationRequest request) {
        service.createConfiguration(request);
        return ResponseEntity.ok("Configuración creada correctamente");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody UConfigurationRequest request) {
        request.setIdconfiguration(id);
        service.updateConfiguration(request);
        return ResponseEntity.ok("Configuración actualizada correctamente");
    }

    @DeleteMapping("{idconfiguration}")
    public ResponseEntity<?> delete(@PathVariable Integer idconfiguration) {
        service.deleteConfiguration(idconfiguration);
        return ResponseEntity.ok("Configuración eliminada correctamente");
    }

    @GetMapping("")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.listConfiguration());
    }
}

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

import com.app.uteq.Dtos.CPermissionRequest;
import com.app.uteq.Dtos.UPermissionRequest;
import com.app.uteq.Services.IPermissionsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
public class PermissionController {
    private final IPermissionsService service;

    // CREATE -> spi_permission
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CPermissionRequest request) {
        service.createPermission(request);
        return ResponseEntity.ok("Permiso creado correctamente");
    }

    // UPDATE -> spu_permission
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody UPermissionRequest request) {
        request.setIdpermission(id);
        service.updatePermission(request);
        return ResponseEntity.ok("Permiso actualizado correctamente");
    }

    // DELETE -> spd_permission (físico)
    @DeleteMapping("/{idpermission}")
    public ResponseEntity<?> delete(@PathVariable Integer idpermission) {
        service.deletePermission(idpermission);
        return ResponseEntity.ok("Permiso eliminado correctamente");
    }

    // LIST -> fn_list_permissions
    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.listPermission());
    }
}

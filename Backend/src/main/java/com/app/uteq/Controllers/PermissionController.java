package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CPermissionRequest;
import com.app.uteq.Dtos.UPermissionRequest;
import com.app.uteq.Services.IPermissionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final IPermissionsService service;

    // CREATE -> spi_permission
    @PostMapping("/sp-create")
    public ResponseEntity<?> create(@RequestBody CPermissionRequest request) {
        service.createPermission(request);
        return ResponseEntity.ok("Permiso creado correctamente");
    }

    // UPDATE -> spu_permission
    @PutMapping("/sp-update")
    public ResponseEntity<?> update(@RequestBody UPermissionRequest request) {
        service.updatePermission(request);
        return ResponseEntity.ok("Permiso actualizado correctamente");
    }

    // DELETE -> spd_permission (físico)
    @DeleteMapping("/sp-delete/{idpermission}")
    public ResponseEntity<?> delete(@PathVariable Integer idpermission) {
        service.deletePermission(idpermission);
        return ResponseEntity.ok("Permiso eliminado correctamente");
    }

    // LIST -> fn_list_permissions
    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.listPermission());
    }
}

package com.app.uteq.Controllers;

import com.app.uteq.Dtos.CRoleRequest;
import com.app.uteq.Dtos.RoleResponse;
import com.app.uteq.Dtos.URoleRequest;
import com.app.uteq.Entity.Roles;
import com.app.uteq.Services.IRolesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
public class RolesController {
    
    private final IRolesService service;

    // ═════════════════════════════════════════════════════════════════
    // ENDPOINTS LEGACY
    // ═════════════════════════════════════════════════════════════════

    @GetMapping("/legacy")
    public ResponseEntity<List<Roles>> findAllLegacy() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/legacy/{id}")
    public ResponseEntity<Roles> findByIdLegacy(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ═════════════════════════════════════════════════════════════════
    // NUEVOS ENDPOINTS CON DTOs Y VALIDACIÓN
    // ═════════════════════════════════════════════════════════════════

    @GetMapping
    public ResponseEntity<List<RoleResponse>> findAll() {
        return ResponseEntity.ok(service.findAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findRoleById(id));
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<RoleResponse> findByName(@PathVariable String roleName) {
        return service.findByRoleName(roleName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createRole(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody URoleRequest request) {
        return ResponseEntity.ok(service.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    // ═════════════════════════════════════════════════════════════════
    // GESTIÓN DE PERMISOS
    // ═════════════════════════════════════════════════════════════════

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<RoleResponse> assignPermissions(
            @PathVariable Integer roleId,
            @RequestBody Set<Integer> permissionIds) {
        return ResponseEntity.ok(service.assignPermissions(roleId, permissionIds));
    }

    @DeleteMapping("/{roleId}/permissions")
    public ResponseEntity<RoleResponse> removePermissions(
            @PathVariable Integer roleId,
            @RequestBody Set<Integer> permissionIds) {
        return ResponseEntity.ok(service.removePermissions(roleId, permissionIds));
    }

    // ═════════════════════════════════════════════════════════════════
    // ASIGNACIÓN DE ROLES A USUARIOS
    // ═════════════════════════════════════════════════════════════════

    @PostMapping("/{roleId}/users/{userId}")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable Integer roleId,
            @PathVariable Integer userId) {
        service.assignRoleToUser(roleId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}/users/{userId}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable Integer roleId,
            @PathVariable Integer userId) {
        service.removeRoleFromUser(roleId, userId);
        return ResponseEntity.noContent().build();
    }
}

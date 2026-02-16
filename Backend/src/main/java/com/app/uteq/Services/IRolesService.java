package com.app.uteq.Services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.app.uteq.Dtos.CRoleRequest;
import com.app.uteq.Dtos.RoleResponse;
import com.app.uteq.Dtos.URoleRequest;
import com.app.uteq.Entity.Roles;

public interface IRolesService {

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═════════════════════════════════════════════════════════════
    List<Roles> findAll();
    Optional<Roles> findById(Integer id);
    Roles save(Roles role);
    void deleteById(Integer id);

    // ═════════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═════════════════════════════════════════════════════════════

    /**
     * Obtiene todos los roles como DTOs.
     */
    List<RoleResponse> findAllRoles();

    /**
     * Busca un rol por ID.
     * @throws ResourceNotFoundException si no existe
     */
    RoleResponse findRoleById(Integer id);

    /**
     * Busca un rol por nombre.
     */
    Optional<RoleResponse> findByRoleName(String roleName);

    /**
     * Crea un nuevo rol.
     * @throws DuplicateResourceException si el nombre ya existe
     */
    RoleResponse createRole(CRoleRequest request);

    /**
     * Actualiza un rol existente.
     * @throws ResourceNotFoundException si no existe
     * @throws DuplicateResourceException si el nuevo nombre ya existe
     */
    RoleResponse updateRole(Integer id, URoleRequest request);

    /**
     * Elimina un rol.
     * @throws BusinessException si el rol está asignado a usuarios
     */
    void deleteRole(Integer id);

    /**
     * Asigna permisos a un rol.
     */
    RoleResponse assignPermissions(Integer roleId, Set<Integer> permissionIds);

    /**
     * Remueve permisos de un rol.
     */
    RoleResponse removePermissions(Integer roleId, Set<Integer> permissionIds);

    /**
     * Asigna un rol a un usuario.
     */
    void assignRoleToUser(Integer roleId, Integer userId);

    /**
     * Remueve un rol de un usuario.
     */
    void removeRoleFromUser(Integer roleId, Integer userId);
}

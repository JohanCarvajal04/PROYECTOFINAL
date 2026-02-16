package com.app.uteq.Services.Impl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CRoleRequest;
import com.app.uteq.Dtos.RoleResponse;
import com.app.uteq.Dtos.URoleRequest;
import com.app.uteq.Entity.Permissions;
import com.app.uteq.Entity.Roles;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.BadRequestException;
import com.app.uteq.Exceptions.BusinessException;
import com.app.uteq.Exceptions.DuplicateResourceException;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Repository.IPermissionsRepository;
import com.app.uteq.Repository.IRolesRepository;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.IRolesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RolesServiceImpl implements IRolesService {

    private final IRolesRepository repository;
    private final IPermissionsRepository permissionsRepository;
    private final IUsersRepository usersRepository;

    // Roles del sistema que no pueden ser eliminados
    private static final Set<String> PROTECTED_ROLES = Set.of(
            "ROLE_ADMIN", "ROLE_STUDENT", "ROLE_COORDINATOR", "ROLE_DEAN"
    );

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Roles> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Roles> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Roles save(Roles role) {
        return repository.save(role);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    // ═════════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> findAllRoles() {
        return repository.findAll().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findRoleById(Integer id) {
        Roles role = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", id));
        return toRoleResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleResponse> findByRoleName(String roleName) {
        return repository.findByRoleName(roleName)
                .map(this::toRoleResponse);
    }

    @Override
    public RoleResponse createRole(CRoleRequest request) {
        // Validar formato del nombre
        validateRoleName(request.getRoleName());

        // Validar unicidad
        if (repository.existsByRoleName(request.getRoleName())) {
            throw new DuplicateResourceException("Rol", "nombre", request.getRoleName());
        }

        Roles role = Roles.builder()
                .roleName(request.getRoleName().toUpperCase())
                .roleDescription(request.getRoleDescription())
                .permissions(new HashSet<>())
                .build();

        Roles saved = repository.save(role);
        return toRoleResponse(saved);
    }

    @Override
    public RoleResponse updateRole(Integer id, URoleRequest request) {
        Roles role = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", id));

        // Validar que no sea un rol protegido cambiando de nombre
        if (PROTECTED_ROLES.contains(role.getRoleName()) && 
            !role.getRoleName().equals(request.getRoleName())) {
            throw new BusinessException("PROTECTED_ROLE", 
                    "No se puede cambiar el nombre de un rol del sistema");
        }

        // Validar nombre si cambió
        if (!role.getRoleName().equals(request.getRoleName())) {
            validateRoleName(request.getRoleName());
            if (repository.existsByRoleName(request.getRoleName())) {
                throw new DuplicateResourceException("Rol", "nombre", request.getRoleName());
            }
            role.setRoleName(request.getRoleName().toUpperCase());
        }

        if (request.getRoleDescription() != null) {
            role.setRoleDescription(request.getRoleDescription());
        }

        Roles saved = repository.save(role);
        return toRoleResponse(saved);
    }

    @Override
    public void deleteRole(Integer id) {
        Roles role = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", id));

        // Validar que no sea un rol protegido
        if (PROTECTED_ROLES.contains(role.getRoleName())) {
            throw new BusinessException("PROTECTED_ROLE", 
                    "No se puede eliminar un rol del sistema: " + role.getRoleName());
        }

        // Validar que no tenga usuarios asignados
        List<Users> usersWithRole = usersRepository.findByRolesIdRole(id);
        if (!usersWithRole.isEmpty()) {
            throw new BusinessException("ROLE_IN_USE", 
                    "No se puede eliminar el rol porque está asignado a " + 
                    usersWithRole.size() + " usuario(s)");
        }

        repository.deleteById(id);
    }

    @Override
    public RoleResponse assignPermissions(Integer roleId, Set<Integer> permissionIds) {
        Roles role = repository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", roleId));

        Set<Permissions> permissions = permissionIds.stream()
                .map(permId -> permissionsRepository.findById(permId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permiso", "id", permId)))
                .collect(Collectors.toSet());

        role.getPermissions().addAll(permissions);
        Roles saved = repository.save(role);
        return toRoleResponse(saved);
    }

    @Override
    public RoleResponse removePermissions(Integer roleId, Set<Integer> permissionIds) {
        Roles role = repository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", roleId));

        role.getPermissions().removeIf(p -> permissionIds.contains(p.getIdPermission()));
        Roles saved = repository.save(role);
        return toRoleResponse(saved);
    }

    @Override
    public void assignRoleToUser(Integer roleId, Integer userId) {
        Roles role = repository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", roleId));

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (user.getRoles().contains(role)) {
            throw new BusinessException("ROLE_ALREADY_ASSIGNED", 
                    "El usuario ya tiene asignado este rol");
        }

        user.getRoles().add(role);
        usersRepository.save(user);
    }

    @Override
    public void removeRoleFromUser(Integer roleId, Integer userId) {
        Roles role = repository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", roleId));

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (!user.getRoles().contains(role)) {
            throw new BusinessException("ROLE_NOT_ASSIGNED", 
                    "El usuario no tiene asignado este rol");
        }

        // Verificar que el usuario mantenga al menos un rol
        if (user.getRoles().size() <= 1) {
            throw new BusinessException("LAST_ROLE", 
                    "No se puede remover el último rol del usuario");
        }

        user.getRoles().remove(role);
        usersRepository.save(user);
    }

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ═════════════════════════════════════════════════════════════

    private void validateRoleName(String roleName) {
        if (roleName == null || !roleName.matches("^ROLE_[A-Z_]+$")) {
            throw new BadRequestException(
                    "El nombre del rol debe iniciar con 'ROLE_' seguido de letras mayúsculas y guiones bajos");
        }
    }

    private RoleResponse toRoleResponse(Roles role) {
        List<RoleResponse.PermissionInfo> permissionInfos = role.getPermissions() != null ?
                role.getPermissions().stream()
                        .map(p -> RoleResponse.PermissionInfo.builder()
                                .idPermission(p.getIdPermission())
                                .code(p.getCode())
                                .description(p.getDescription())
                                .build())
                        .collect(Collectors.toList()) : List.of();

        return RoleResponse.builder()
                .idRole(role.getIdRole())
                .roleName(role.getRoleName())
                .roleDescription(role.getRoleDescription())
                .permissions(permissionInfos)
                .build();
    }
}

package com.app.uteq.Services;

import java.util.List;
import java.util.Optional;

import com.app.uteq.Dtos.CUserRequest;
import com.app.uteq.Dtos.UUserRequest;
import com.app.uteq.Dtos.UserResponse;
import com.app.uteq.Entity.Users;

public interface IUsersService {

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS LEGACY (mantener compatibilidad)
    // ═══════════════════════════════════════════════════════════
    List<Users> findAll();
    Optional<Users> findById(Integer id);
    Users save(Users user);
    void deleteById(Integer id);

    // ═══════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON DTOs Y LÓGICA DE NEGOCIO
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtiene todos los usuarios activos como DTOs.
     */
    List<UserResponse> findAllUsers();

    /**
     * Busca un usuario por ID y retorna como DTO.
     * @throws ResourceNotFoundException si no existe
     */
    UserResponse findUserById(Integer id);

    /**
     * Crea un nuevo usuario validando reglas de negocio.
     * @throws DuplicateResourceException si el email o cédula ya existen
     */
    UserResponse createUser(CUserRequest request);

    /**
     * Actualiza un usuario existente.
     * @throws ResourceNotFoundException si no existe
     * @throws DuplicateResourceException si el nuevo email/cédula ya están en uso
     */
    UserResponse updateUser(Integer id, UUserRequest request);

    /**
     * Elimina un usuario (borrado lógico - soft delete).
     */
    void deleteUser(Integer id);

    /**
     * Desactiva una cuenta de usuario.
     */
    UserResponse deactivateUser(Integer id);

    /**
     * Activa una cuenta de usuario.
     */
    UserResponse activateUser(Integer id);

    /**
     * Busca usuario por email institucional.
     */
    Optional<UserResponse> findByInstitutionalEmail(String email);
}

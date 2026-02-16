package com.app.uteq.Services;

import java.util.List;
import java.util.Optional;

import com.app.uteq.Dtos.CCredentialRequest;
import com.app.uteq.Dtos.CredentialResponse;
import com.app.uteq.Entity.Credentials;

public interface ICredentialsService {
    List<Credentials> findAll();
    Optional<Credentials> findById(Integer id);
    Credentials save(Credentials credentials);
    void deleteById(Integer id);

    // ═══════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtiene todas las credenciales como DTOs (sin exponer passwordHash).
     */
    List<CredentialResponse> findAllCredentials();

    /**
     * Busca credenciales por ID.
     * @throws ResourceNotFoundException si no existe
     */
    CredentialResponse findCredentialById(Integer id);

    /**
     * Crea nuevas credenciales con contraseña encriptada.
     * @throws BadRequestException si la contraseña no cumple requisitos
     */
    CredentialResponse createCredential(CCredentialRequest request);

    /**
     * Cambia la contraseña de un usuario.
     * @throws ResourceNotFoundException si no existe
     * @throws BadRequestException si la contraseña actual es incorrecta
     */
    CredentialResponse changePassword(Integer id, String currentPassword, String newPassword);

    /**
     * Bloquea una cuenta de usuario.
     */
    CredentialResponse lockAccount(Integer id);

    /**
     * Desbloquea una cuenta de usuario y reinicia los intentos fallidos.
     */
    CredentialResponse unlockAccount(Integer id);

    /**
     * Registra un intento de login fallido.
     * Bloquea la cuenta si supera el límite de intentos.
     * @return true si la cuenta fue bloqueada
     */
    boolean registerFailedAttempt(Integer id);

    /**
     * Registra un login exitoso (reinicia intentos, actualiza lastLogin).
     */
    void registerSuccessfulLogin(Integer id);

    /**
     * Reinicia la contraseña a una temporal.
     * @return la nueva contraseña temporal generada
     */
    String resetPassword(Integer id);

    /**
     * Verifica si la contraseña ha expirado.
     */
    boolean isPasswordExpired(Integer id);

    /**
     * Verifica que el usuario autenticado es dueño de la credencial.
     * @throws UnauthorizedException si no es el dueño ni un ADMIN
     */
    void verifyCredentialOwnership(Integer credentialId, String authenticatedEmail);
}

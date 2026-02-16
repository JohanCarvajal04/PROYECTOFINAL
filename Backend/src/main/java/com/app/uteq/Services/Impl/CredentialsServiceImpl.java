package com.app.uteq.Services.Impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CCredentialRequest;
import com.app.uteq.Dtos.CredentialResponse;
import com.app.uteq.Entity.Credentials;
import com.app.uteq.Exceptions.BadRequestException;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Exceptions.UnauthorizedException;
import com.app.uteq.Repository.ICredentialsRepository;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.ICredentialsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CredentialsServiceImpl implements ICredentialsService {

    private final ICredentialsRepository credentialsRepository;
    private final IUsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    // Configuración de seguridad
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Credentials> findAll() {
        return credentialsRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Credentials> findById(Integer id) {
        return credentialsRepository.findById(id);
    }

    @Override
    public Credentials save(Credentials credentials) {
        return credentialsRepository.save(credentials);
    }

    @Override
    public void deleteById(Integer id) {
        credentialsRepository.deleteById(id);
    }

    // ═══════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<CredentialResponse> findAllCredentials() {
        return credentialsRepository.findAll().stream()
                .map(this::toCredentialResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CredentialResponse findCredentialById(Integer id) {
        Credentials credentials = credentialsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial", "id", id));
        return toCredentialResponse(credentials);
    }

    @Override
    public CredentialResponse createCredential(CCredentialRequest request) {
        // Validar fortaleza de contraseña
        validatePasswordStrength(request.getPasswordHash());

        Credentials credentials = Credentials.builder()
                .passwordHash(passwordEncoder.encode(request.getPasswordHash()))
                .dateModification(LocalDateTime.now())
                .failedAttempts(0)
                .accountLocked(false)
                .passwordExpiryDate(LocalDate.now().plusDays(PASSWORD_EXPIRY_DAYS))
                .build();

        Credentials saved = credentialsRepository.save(credentials);
        return toCredentialResponse(saved);
    }

    @Override
    public CredentialResponse changePassword(Integer id, String currentPassword, String newPassword) {
        Credentials credentials = credentialsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial", "id", id));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(currentPassword, credentials.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(newPassword, credentials.getPasswordHash())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }

        // Validar fortaleza
        validatePasswordStrength(newPassword);

        credentials.setPasswordHash(passwordEncoder.encode(newPassword));
        credentials.setDateModification(LocalDateTime.now());
        credentials.setPasswordExpiryDate(LocalDate.now().plusDays(PASSWORD_EXPIRY_DAYS));

        Credentials saved = credentialsRepository.save(credentials);
        return toCredentialResponse(saved);
    }

    @Override
    public CredentialResponse lockAccount(Integer id) {
        Credentials credentials = credentialsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial", "id", id));

        credentials.setAccountLocked(true);
        Credentials saved = credentialsRepository.save(credentials);
        return toCredentialResponse(saved);
    }

    @Override
    public CredentialResponse unlockAccount(Integer id) {
        Credentials credentials = credentialsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial", "id", id));

        credentials.setAccountLocked(false);
        credentials.setFailedAttempts(0);
        Credentials saved = credentialsRepository.save(credentials);
        return toCredentialResponse(saved);
    }

    @Override
    public boolean registerFailedAttempt(Integer id) {
        Credentials credentials = credentialsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial", "id", id));

        int newFailedAttempts = credentials.getFailedAttempts() + 1;
        credentials.setFailedAttempts(newFailedAttempts);

        boolean shouldLock = newFailedAttempts >= MAX_FAILED_ATTEMPTS;
        if (shouldLock) {
            credentials.setAccountLocked(true);
        }

        credentialsRepository.save(credentials);
        return shouldLock;
    }

    @Override
    public void registerSuccessfulLogin(Integer id) {
        Credentials credentials = credentialsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial", "id", id));

        credentials.setFailedAttempts(0);
        credentials.setLastLogin(LocalDateTime.now());
        credentialsRepository.save(credentials);
    }

    @Override
    public String resetPassword(Integer id) {
        Credentials credentials = credentialsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial", "id", id));

        // Generar contraseña temporal
        String tempPassword = generateTemporaryPassword();

        credentials.setPasswordHash(passwordEncoder.encode(tempPassword));
        credentials.setDateModification(LocalDateTime.now());
        credentials.setPasswordExpiryDate(LocalDate.now().plusDays(1)); // Expira en 1 día
        credentials.setAccountLocked(false);
        credentials.setFailedAttempts(0);

        credentialsRepository.save(credentials);
        return tempPassword;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordExpired(Integer id) {
        Credentials credentials = credentialsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial", "id", id));

        if (credentials.getPasswordExpiryDate() == null) {
            return false;
        }

        return LocalDate.now().isAfter(credentials.getPasswordExpiryDate());
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyCredentialOwnership(Integer credentialId, String authenticatedEmail) {
        var user = usersRepository.findByInstitutionalEmail(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", authenticatedEmail));

        // Admins pueden cambiar cualquier contraseña
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleName()));
        if (isAdmin) return;

        // Verificar que la credencial pertenezca al usuario autenticado
        if (user.getCredentials() == null || !user.getCredentials().getId().equals(credentialId)) {
            throw new UnauthorizedException("No tiene permisos para modificar esta credencial");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ═══════════════════════════════════════════════════════════

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("La contraseña debe tener al menos 8 caracteres");
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "@$!%*?&#".indexOf(ch) >= 0);

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
            throw new BadRequestException(
                    "La contraseña debe contener al menos: una mayúscula, una minúscula, " +
                    "un número y un carácter especial (@$!%*?&#)");
        }
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    private CredentialResponse toCredentialResponse(Credentials credentials) {
        return new CredentialResponse(
                credentials.getId(),
                null, // No exponemos username aquí, viene del User
                credentials.getLastLogin(),
                credentials.getFailedAttempts(),
                credentials.getAccountLocked(),
                credentials.getPasswordExpiryDate()
        );
    }

    @Override
    public boolean registerFailedAttemptByEmail(String email) {
        var user = usersRepository.findByInstitutionalEmail(email).orElse(null);
        if (user == null || user.getCredentials() == null) {
            return false;
        }
        return registerFailedAttempt(user.getCredentials().getId());
    }

    @Override
    public void registerSuccessfulLoginByEmail(String email) {
        var user = usersRepository.findByInstitutionalEmail(email).orElse(null);
        if (user == null || user.getCredentials() == null) {
            return;
        }
        registerSuccessfulLogin(user.getCredentials().getId());
    }
}

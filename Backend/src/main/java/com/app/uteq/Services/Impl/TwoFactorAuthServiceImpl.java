package com.app.uteq.Services.Impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.TwoFactorAuthResponse;
import com.app.uteq.Dtos.TwoFactorSetupResponse;
import com.app.uteq.Entity.Credentials;
import com.app.uteq.Entity.TwoFactorAuth;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.BadRequestException;
import com.app.uteq.Exceptions.BusinessException;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Repository.ITwoFactorAuthRepository;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.ITwoFactorAuthService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TwoFactorAuthServiceImpl implements ITwoFactorAuthService {

    private final ITwoFactorAuthRepository twoFactorAuthRepository;
    private final IUsersRepository usersRepository;

    private static final String ISSUER = "SGTE-UTEQ";
    private static final int BACKUP_CODE_COUNT = 8;
    private static final int BACKUP_CODE_LENGTH = 8;

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    // ═══════════════════════════════════════════════════════════
    // CRUD BASE
    // ═══════════════════════════════════════════════════════════

    @Override
    public List<TwoFactorAuth> findAll() {
        return twoFactorAuthRepository.findAll();
    }

    @Override
    public Optional<TwoFactorAuth> findById(Integer id) {
        return twoFactorAuthRepository.findById(id);
    }

    @Override
    public TwoFactorAuth save(TwoFactorAuth twoFactorAuth) {
        return twoFactorAuthRepository.save(twoFactorAuth);
    }

    @Override
    public void deleteById(Integer id) {
        twoFactorAuthRepository.deleteById(id);
    }

    // ═══════════════════════════════════════════════════════════
    // 2FA OPERATIONS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public TwoFactorSetupResponse setup2FA(String email) {
        Users user = findUserByEmail(email);
        Credentials credentials = getCredentials(user);

        // Si ya tiene 2FA activo, no se puede configurar de nuevo sin desactivar primero
        Optional<TwoFactorAuth> existing = twoFactorAuthRepository.findByCredentials_Id(credentials.getId());
        if (existing.isPresent() && existing.get().getEnabled()) {
            throw new BusinessException("2FA ya está activo. Desactívelo primero para reconfigurarlo.");
        }

        // Generar secreto con GoogleAuthenticator
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secretKey = key.getKey();

        // Generar URI otpauth:// para código QR
        String qrCodeUri = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                ISSUER, email, secretKey, ISSUER
        );

        // Generar códigos de respaldo
        List<String> backupCodes = generateBackupCodesList();

        // Crear o actualizar registro de 2FA
        TwoFactorAuth twoFactorAuth;
        if (existing.isPresent()) {
            twoFactorAuth = existing.get();
        } else {
            twoFactorAuth = TwoFactorAuth.builder()
                    .credentials(credentials)
                    .build();
        }
        twoFactorAuth.setSecretKey(secretKey);
        twoFactorAuth.setBackupCodes(backupCodes);
        twoFactorAuth.setEnabled(false); // Se activa al verificar
        twoFactorAuth.setVerifiedAt(null);

        twoFactorAuthRepository.save(twoFactorAuth);

        return TwoFactorSetupResponse.builder()
                .secretKey(secretKey)
                .qrCodeUri(qrCodeUri)
                .backupCodes(backupCodes)
                .build();
    }

    @Override
    @Transactional
    public boolean verifyAndEnable2FA(String email, int code) {
        Users user = findUserByEmail(email);
        Credentials credentials = getCredentials(user);

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByCredentials_Id(credentials.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TwoFactorAuth", "credentialsId", credentials.getId().toString()));

        if (twoFactorAuth.getEnabled()) {
            throw new BusinessException("2FA ya está verificado y activo.");
        }

        if (twoFactorAuth.getSecretKey() == null) {
            throw new BusinessException("Primero debe ejecutar /setup para generar la clave secreta.");
        }

        // Validar el código TOTP
        boolean isValid = googleAuthenticator.authorize(twoFactorAuth.getSecretKey(), code);
        if (!isValid) {
            throw new BadRequestException("Código TOTP inválido. Verifique e intente nuevamente.");
        }

        // Activar 2FA
        twoFactorAuth.setEnabled(true);
        twoFactorAuth.setVerifiedAt(LocalDateTime.now());
        twoFactorAuthRepository.save(twoFactorAuth);

        return true;
    }

    @Override
    @Transactional
    public void disable2FA(String email, int code) {
        Users user = findUserByEmail(email);
        Credentials credentials = getCredentials(user);

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByCredentials_Id(credentials.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TwoFactorAuth", "credentialsId", credentials.getId().toString()));

        if (!twoFactorAuth.getEnabled()) {
            throw new BusinessException("2FA no está activo.");
        }

        // Validar código TOTP antes de desactivar
        boolean isValid = googleAuthenticator.authorize(twoFactorAuth.getSecretKey(), code);
        if (!isValid) {
            throw new BadRequestException("Código TOTP inválido. No se puede desactivar 2FA.");
        }

        twoFactorAuth.setEnabled(false);
        twoFactorAuth.setSecretKey(null);
        twoFactorAuth.setBackupCodes(null);
        twoFactorAuth.setVerifiedAt(null);
        twoFactorAuthRepository.save(twoFactorAuth);
    }

    @Override
    public boolean validateCode(String email, int code) {
        Users user = findUserByEmail(email);
        Credentials credentials = getCredentials(user);

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByCredentials_Id(credentials.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TwoFactorAuth", "credentialsId", credentials.getId().toString()));

        if (!twoFactorAuth.getEnabled()) {
            throw new BusinessException("2FA no está activo para este usuario.");
        }

        return googleAuthenticator.authorize(twoFactorAuth.getSecretKey(), code);
    }

    @Override
    @Transactional
    public boolean validateBackupCode(String email, String backupCode) {
        Users user = findUserByEmail(email);
        Credentials credentials = getCredentials(user);

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByCredentials_Id(credentials.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TwoFactorAuth", "credentialsId", credentials.getId().toString()));

        if (!twoFactorAuth.getEnabled()) {
            throw new BusinessException("2FA no está activo para este usuario.");
        }

        List<String> codes = twoFactorAuth.getBackupCodes();
        if (codes == null || !codes.contains(backupCode.trim())) {
            return false;
        }

        // Consumir el código de respaldo (un solo uso)
        codes.remove(backupCode.trim());
        twoFactorAuth.setBackupCodes(codes);
        twoFactorAuthRepository.save(twoFactorAuth);

        return true;
    }

    @Override
    public boolean is2FAEnabled(String email) {
        Users user = findUserByEmail(email);
        Credentials credentials = getCredentials(user);
        return twoFactorAuthRepository.existsByCredentials_IdAndEnabledTrue(credentials.getId());
    }

    @Override
    public TwoFactorAuthResponse getStatus(String email) {
        Users user = findUserByEmail(email);
        Credentials credentials = getCredentials(user);

        Optional<TwoFactorAuth> twoFactorAuth = twoFactorAuthRepository.findByCredentials_Id(credentials.getId());

        if (twoFactorAuth.isEmpty()) {
            return TwoFactorAuthResponse.builder()
                    .enabled(false)
                    .verifiedAt(null)
                    .build();
        }

        TwoFactorAuth tfa = twoFactorAuth.get();
        return TwoFactorAuthResponse.builder()
                .enabled(tfa.getEnabled())
                .verifiedAt(tfa.getVerifiedAt())
                .build();
    }

    @Override
    @Transactional
    public List<String> regenerateBackupCodes(String email, int code) {
        Users user = findUserByEmail(email);
        Credentials credentials = getCredentials(user);

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByCredentials_Id(credentials.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TwoFactorAuth", "credentialsId", credentials.getId().toString()));

        if (!twoFactorAuth.getEnabled()) {
            throw new BusinessException("2FA no está activo. No se pueden regenerar códigos.");
        }

        // Validar código TOTP antes de regenerar
        boolean isValid = googleAuthenticator.authorize(twoFactorAuth.getSecretKey(), code);
        if (!isValid) {
            throw new BadRequestException("Código TOTP inválido.");
        }

        List<String> newBackupCodes = generateBackupCodesList();
        twoFactorAuth.setBackupCodes(newBackupCodes);
        twoFactorAuthRepository.save(twoFactorAuth);

        return newBackupCodes;
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS PRIVADOS
    // ═══════════════════════════════════════════════════════════

    private Users findUserByEmail(String email) {
        return usersRepository.findByInstitutionalEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
    }

    private Credentials getCredentials(Users user) {
        if (user.getCredentials() == null) {
            throw new BusinessException("El usuario no tiene credenciales configuradas.");
        }
        return user.getCredentials();
    }

    private List<String> generateBackupCodesList() {
        SecureRandom random = new SecureRandom();
        List<String> codes = new ArrayList<>();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
            codes.add(code.toString());
        }
        return codes;
    }
}

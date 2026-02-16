package com.app.uteq.Controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.TwoFactorAuthResponse;
import com.app.uteq.Dtos.TwoFactorBackupRequest;
import com.app.uteq.Dtos.TwoFactorSetupResponse;
import com.app.uteq.Dtos.TwoFactorVerifyRequest;
import com.app.uteq.Services.ITwoFactorAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/2fa")
public class TwoFactorAuthController {

    private final ITwoFactorAuthService twoFactorAuthService;

    // ═══════════════════════════════════════════════════════════
    // CONFIGURACIÓN (requiere JWT)
    // ═══════════════════════════════════════════════════════════

    /**
     * Genera la clave secreta, URI para QR y códigos de respaldo.
     * Se muestra UNA SOLA VEZ al usuario.
     */
    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TwoFactorSetupResponse> setup(Authentication authentication) {
        String email = authentication.getName();
        TwoFactorSetupResponse response = twoFactorAuthService.setup2FA(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Primera verificación del código TOTP → activa 2FA.
     */
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> verify(
            Authentication authentication,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        String email = authentication.getName();
        twoFactorAuthService.verifyAndEnable2FA(email, request.getCode());
        return ResponseEntity.ok(Map.of(
                "message", "2FA activado exitosamente",
                "enabled", true
        ));
    }

    /**
     * Desactiva 2FA (requiere código TOTP válido).
     */
    @DeleteMapping("/disable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> disable(
            Authentication authentication,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        String email = authentication.getName();
        twoFactorAuthService.disable2FA(email, request.getCode());
        return ResponseEntity.ok(Map.of(
                "message", "2FA desactivado exitosamente",
                "enabled", false
        ));
    }

    /**
     * Consulta el estado de 2FA del usuario autenticado.
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TwoFactorAuthResponse> status(Authentication authentication) {
        String email = authentication.getName();
        TwoFactorAuthResponse response = twoFactorAuthService.getStatus(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Regenera códigos de respaldo (requiere código TOTP válido).
     */
    @PostMapping("/backup-codes/regenerate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> regenerateBackupCodes(
            Authentication authentication,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        String email = authentication.getName();
        List<String> newCodes = twoFactorAuthService.regenerateBackupCodes(email, request.getCode());
        return ResponseEntity.ok(Map.of(
                "message", "Códigos de respaldo regenerados exitosamente",
                "backupCodes", newCodes
        ));
    }

    // ═══════════════════════════════════════════════════════════
    // VALIDACIÓN DURANTE LOGIN (rutas públicas)
    // ═══════════════════════════════════════════════════════════

    /**
     * Valida un código TOTP durante el flujo de login con 2FA.
     * Se usa junto con el pre_auth_token del AuthController.
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCode(
            @RequestParam String email,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        boolean valid = twoFactorAuthService.validateCode(email, request.getCode());
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    /**
     * Valida un código de respaldo durante el flujo de login con 2FA.
     */
    @PostMapping("/validate-backup")
    public ResponseEntity<Map<String, Object>> validateBackupCode(
            @RequestParam String email,
            @Valid @RequestBody TwoFactorBackupRequest request) {
        boolean valid = twoFactorAuthService.validateBackupCode(email, request.getBackupCode());
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}

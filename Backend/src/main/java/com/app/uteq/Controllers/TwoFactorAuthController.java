package com.app.uteq.Controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
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
    private final JwtDecoder jwtDecoder;

    // ═══════════════════════════════════════════════════════════
    // CONFIGURACIÓN (requiere JWT)
    // ═══════════════════════════════════════════════════════════

    /**
     * Genera la clave secreta, URI para QR y códigos de respaldo.
     * Se muestra UNA SOLA VEZ al usuario.
     */
    @PostMapping("/setup")
    @PreAuthorize("hasAuthority('AUTH2FA_CONFIGURAR')")
    public ResponseEntity<TwoFactorSetupResponse> setup(Authentication authentication) {
        String email = authentication.getName();
        TwoFactorSetupResponse response = twoFactorAuthService.setup2FA(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Primera verificación del código TOTP → activa 2FA.
     */
    @PostMapping("/verify")
    @PreAuthorize("hasAuthority('AUTH2FA_VERIFICAR')")
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
    @PreAuthorize("hasAuthority('AUTH2FA_DESACTIVAR')")
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
    @PreAuthorize("hasAuthority('AUTH2FA_ESTADO')")
    public ResponseEntity<TwoFactorAuthResponse> status(Authentication authentication) {
        String email = authentication.getName();
        TwoFactorAuthResponse response = twoFactorAuthService.getStatus(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Regenera códigos de respaldo (requiere código TOTP válido).
     */
    @PostMapping("/backup-codes/regenerate")
    @PreAuthorize("hasAuthority('AUTH2FA_REGENERAR')")
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
     * Requiere el pre_auth_token emitido por AuthController (evita enumeración de usuarios).
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCode(
            @RequestParam String preAuthToken,
            @Valid @RequestBody TwoFactorVerifyRequest request) {

        String email = extractEmailFromPreAuthToken(preAuthToken);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Pre-auth token inválido o expirado"));
        }

        boolean valid = twoFactorAuthService.validateCode(email, request.getCode());
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    /**
     * Valida un código de respaldo durante el flujo de login con 2FA.
     * Requiere el pre_auth_token emitido por AuthController (evita enumeración de usuarios).
     */
    @PostMapping("/validate-backup")
    public ResponseEntity<Map<String, Object>> validateBackupCode(
            @RequestParam String preAuthToken,
            @Valid @RequestBody TwoFactorBackupRequest request) {

        String email = extractEmailFromPreAuthToken(preAuthToken);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Pre-auth token inválido o expirado"));
        }

        boolean valid = twoFactorAuthService.validateBackupCode(email, request.getBackupCode());
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    /**
     * Extrae el email (subject) del pre_auth_token JWT, validando que sea de tipo "pre_auth".
     */
    private String extractEmailFromPreAuthToken(String preAuthToken) {
        try {
            Jwt decoded = jwtDecoder.decode(preAuthToken);
            String tokenType = decoded.getClaimAsString("token_type");
            if (!"pre_auth".equals(tokenType)) {
                return null;
            }
            return decoded.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
}

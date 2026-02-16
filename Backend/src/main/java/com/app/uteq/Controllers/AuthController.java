package com.app.uteq.Controllers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Entity.RefreshToken;
import com.app.uteq.Entity.Users;
import com.app.uteq.Repository.IRefreshTokenRepository;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.ITwoFactorAuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUsersRepository usersRepository;
    private final ITwoFactorAuthService twoFactorAuthService;

    public AuthController(JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            IRefreshTokenRepository refreshTokenRepository,
            IUsersRepository usersRepository,
            ITwoFactorAuthService twoFactorAuthService) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.usersRepository = usersRepository;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    /**
     * Endpoint para obtener tokens JWT.
     *
     * Soporta dos grant types:
     * - "password": Autenticación con email institucional y contraseña.
     *   Si el usuario tiene 2FA activo, retorna un pre_auth_token en lugar de
     *   tokens completos. El cliente debe usar POST /auth/2fa-verify para completar.
     * - "refresh_token": Renovar access token usando un refresh token.
     *
     * @param grantType        "password" o "refresh_token"
     * @param username         Email institucional (requerido para grantType=password)
     * @param password         Contraseña (requerido para grantType=password)
     * @param withRefreshToken Si se debe generar un refresh token
     * @param refreshToken     El refresh token (requerido para grantType=refresh_token)
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> generateToken(
            @RequestParam String grantType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(defaultValue = "false") boolean withRefreshToken,
            @RequestParam(required = false) String refreshToken) {

        String subject = null;
        String scope = null;

        // ─── Grant Type: Password ──────────────────────────────
        if ("password".equals(grantType)) {
            if (username == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Correo y contraseña son requeridos"));
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, password));

                subject = authentication.getName();
                scope = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));

            } catch (AuthenticationException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas"));
            }

            // ─── Verificar si 2FA está activo ──────────────────────
            boolean requires2FA = false;
            try {
                requires2FA = twoFactorAuthService.is2FAEnabled(subject);
            } catch (Exception e) {
                log.warn("Error al verificar 2FA para {}: {} - Se procede sin 2FA",
                        subject, e.getMessage(), e);
            }

            if (requires2FA) {
                // Generar pre-auth token (5 min, sin scope, no es un access_token válido)
                Instant now = Instant.now();
                JwtClaimsSet preAuthClaims = JwtClaimsSet.builder()
                        .subject(subject)
                        .issuedAt(now)
                        .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                        .issuer("sgte-backend")
                        .claim("token_type", "pre_auth")
                        .claim("requires_2fa", true)
                        .claim("with_refresh_token", withRefreshToken)
                        .build();

                String preAuthToken = jwtEncoder.encode(
                        JwtEncoderParameters.from(preAuthClaims)).getTokenValue();

                Map<String, String> preAuthResponse = new HashMap<>();
                preAuthResponse.put("pre_auth_token", preAuthToken);
                preAuthResponse.put("requires_2fa", "true");
                preAuthResponse.put("message", "Se requiere verificación 2FA");
                return ResponseEntity.ok(preAuthResponse);
            }

            // ─── Grant Type: Refresh Token ─────────────────────────
        } else if ("refresh_token".equals(grantType)) {
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "El refresh token es requerido"));
            }

            try {
                Jwt decodedToken = jwtDecoder.decode(refreshToken);
                subject = decodedToken.getSubject();

                var storedToken = refreshTokenRepository.findByToken(refreshToken);
                if (storedToken.isPresent() && storedToken.get().getRevoked()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "El refresh token ha sido revocado"));
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
                scope = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));

            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token inválido: " + e.getMessage()));
            }

            // ─── Grant Type no soportado ───────────────────────────
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Grant type no soportado: " + grantType));
        }

        // ─── Generar tokens completos ──────────────────────────
        return ResponseEntity.ok(generateFullTokens(subject, scope, withRefreshToken));
    }

    /**
     * Endpoint para completar la autenticación 2FA.
     *
     * Recibe el pre_auth_token (obtenido en /token) y el código TOTP de 6 dígitos.
     * Si el código es válido, retorna access_token + refresh_token completos.
     *
     * @param preAuthToken Token de pre-autenticación obtenido del login
     * @param code         Código TOTP de 6 dígitos de Google Authenticator
     * @param backupCode   Código de respaldo (alternativa al código TOTP)
     */
    @PostMapping("/2fa-verify")
    public ResponseEntity<Map<String, String>> verify2FA(
            @RequestParam String preAuthToken,
            @RequestParam(required = false) Integer code,
            @RequestParam(required = false) String backupCode) {

        // Validar el pre-auth token
        Jwt decoded;
        try {
            decoded = jwtDecoder.decode(preAuthToken);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Pre-auth token inválido o expirado"));
        }

        // Verificar que sea un pre_auth token
        String tokenType = decoded.getClaimAsString("token_type");
        if (!"pre_auth".equals(tokenType)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token no es un pre-auth token válido"));
        }

        String subject = decoded.getSubject();
        Boolean withRefreshToken = decoded.getClaim("with_refresh_token");

        // Validar código TOTP o código de respaldo
        boolean valid = false;

        if (code != null) {
            valid = twoFactorAuthService.validateCode(subject, code);
        } else if (backupCode != null && !backupCode.isBlank()) {
            valid = twoFactorAuthService.validateBackupCode(subject, backupCode);
        }

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Código 2FA inválido"));
        }

        // Recargar authorities del usuario para generar tokens completos
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
        String scope = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        return ResponseEntity.ok(generateFullTokens(subject, scope,
                withRefreshToken != null && withRefreshToken));
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER: Genera access_token + refresh_token
    // ═══════════════════════════════════════════════════════════

    private Map<String, String> generateFullTokens(String subject, String scope, boolean withRefreshToken) {
        Instant now = Instant.now();

        JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .issuer("sgte-backend")
                .claim("scope", scope)
                .claim("token_type", "access_token")
                .build();

        String accessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(accessTokenClaims)).getTokenValue();

        Map<String, String> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("token_type", "Bearer");
        response.put("expires_in", "900");

        if (withRefreshToken) {
            JwtClaimsSet refreshTokenClaims = JwtClaimsSet.builder()
                    .subject(subject)
                    .issuedAt(now)
                    .expiresAt(now.plus(24, ChronoUnit.HOURS))
                    .issuer("sgte-backend")
                    .claim("scope", scope)
                    .claim("token_type", "refresh_token")
                    .build();

            String newRefreshToken = jwtEncoder.encode(
                    JwtEncoderParameters.from(refreshTokenClaims)).getTokenValue();

            Users user = usersRepository.findByInstitutionalEmail(subject)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + subject));
            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .token(newRefreshToken)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .revoked(false)
                    .build();
            refreshTokenRepository.save(refreshTokenEntity);

            response.put("refresh_token", newRefreshToken);
        }

        return response;
    }
}

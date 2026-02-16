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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Entity.RefreshToken;
import com.app.uteq.Entity.Users;
import com.app.uteq.Repository.IRefreshTokenRepository;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.ICredentialsService;
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
    private final ICredentialsService credentialsService;

    public AuthController(JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            IRefreshTokenRepository refreshTokenRepository,
            IUsersRepository usersRepository,
            ITwoFactorAuthService twoFactorAuthService,
            ICredentialsService credentialsService) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.usersRepository = usersRepository;
        this.twoFactorAuthService = twoFactorAuthService;
        this.credentialsService = credentialsService;
    }

    @PostMapping("/token")
    @Transactional
    public ResponseEntity<Map<String, String>> generateToken(
            @RequestParam String grantType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(defaultValue = "false") boolean withRefreshToken,
            @RequestParam(required = false) String refreshToken) {

        String subject = null;
        String scope = null;

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

                // Registrar login exitoso (resetea intentos fallidos)
                credentialsService.registerSuccessfulLoginByEmail(subject);

            } catch (AuthenticationException e) {
                // Registrar intento fallido (puede bloquear la cuenta)
                boolean locked = credentialsService.registerFailedAttemptByEmail(username);
                String errorMsg = locked
                        ? "Cuenta bloqueada por exceder el máximo de intentos fallidos"
                        : "Credenciales inválidas";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", errorMsg));
            }

            boolean requires2FA = false;
            try {
                requires2FA = twoFactorAuthService.is2FAEnabled(subject);
            } catch (Exception e) {
                log.warn("Error al verificar 2FA para {}: {} - Se procede sin 2FA",
                        subject, e.getMessage(), e);
            }

            if (requires2FA) {
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

        } else if ("refresh_token".equals(grantType)) {
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "El refresh token es requerido"));
            }

            try {
                Jwt decodedToken = jwtDecoder.decode(refreshToken);
                subject = decodedToken.getSubject();

                var storedToken = refreshTokenRepository.findByToken(refreshToken);
                if (storedToken.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Refresh token no encontrado"));
                }
                if (storedToken.get().getRevoked()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "El refresh token ha sido revocado"));
                }

                // Rotación de refresh token: revocar el token usado
                RefreshToken tokenEntity = storedToken.get();
                tokenEntity.setRevoked(true);
                refreshTokenRepository.save(tokenEntity);

                UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
                scope = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));

                // Forzar generación de nuevo refresh token en la rotación
                withRefreshToken = true;

            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token inválido: " + e.getMessage()));
            }

        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Grant type no soportado: " + grantType));
        }

        return ResponseEntity.ok(generateFullTokens(subject, scope, withRefreshToken));
    }

    @PostMapping("/2fa-verify")
    @Transactional
    public ResponseEntity<Map<String, String>> verify2FA(
            @RequestParam String preAuthToken,
            @RequestParam(required = false) Integer code,
            @RequestParam(required = false) String backupCode) {

        Jwt decoded;
        try {
            decoded = jwtDecoder.decode(preAuthToken);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Pre-auth token inválido o expirado"));
        }

        String tokenType = decoded.getClaimAsString("token_type");
        if (!"pre_auth".equals(tokenType)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token no es un pre-auth token válido"));
        }

        String subject = decoded.getSubject();
        Boolean withRefreshToken = decoded.getClaim("with_refresh_token");

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

        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
        String scope = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        return ResponseEntity.ok(generateFullTokens(subject, scope,
                withRefreshToken != null && withRefreshToken));
    }

    /**
     * Cierra sesión revocando todos los refresh tokens activos del usuario.
     */
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String email = authentication.getName();
        Users user = usersRepository.findByInstitutionalEmail(email).orElse(null);

        if (user != null) {
            refreshTokenRepository.revokeAllByUser(user);
            log.info("Sesión cerrada para usuario: {}", email);
        }

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));
    }

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

package com.app.uteq.Controllers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUsersRepository usersRepository;

    public AuthController(JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            IRefreshTokenRepository refreshTokenRepository,
            IUsersRepository usersRepository) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.usersRepository = usersRepository;
    }

    /**
     * Endpoint para obtener tokens JWT.
     *
     * Soporta dos grant types:
     * - "password": Autenticación con email institucional y contraseña
     * - "refresh_token": Renovar access token usando un refresh token
     *
     * @param grantType        "password" o "refresh_token"
     * @param username         Email institucional (requerido para
     *                         grantType=password)
     * @param password         Contraseña (requerido para grantType=password)
     * @param withRefreshToken Si se debe generar un refresh token
     * @param refreshToken     El refresh token (requerido para
     *                         grantType=refresh_token)
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

            // ─── Grant Type: Refresh Token ─────────────────────────
        } else if ("refresh_token".equals(grantType)) {
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "El refresh token es requerido"));
            }

            try {
                // Validar que el JWT del refresh token sea válido
                Jwt decodedToken = jwtDecoder.decode(refreshToken);
                subject = decodedToken.getSubject();

                // Verificar en la BD que el refresh token no esté revocado
                var storedToken = refreshTokenRepository.findByToken(refreshToken);
                if (storedToken.isPresent() && storedToken.get().getRevoked()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "El refresh token ha sido revocado"));
                }

                // Recargar authorities del usuario
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

        // ─── Generar Access Token ──────────────────────────────
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

        // ─── Generar Refresh Token (si se solicita) ────────────
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

            // Guardar refresh token en la base de datos
            Users user = usersRepository.findByInstitutionalEmail(subject).orElse(null);
            if (user != null) {
                RefreshToken refreshTokenEntity = RefreshToken.builder()
                        .token(newRefreshToken)
                        .user(user)
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusHours(24))
                        .revoked(false)
                        .build();
                refreshTokenRepository.save(refreshTokenEntity);
            }

            response.put("refresh_token", newRefreshToken);
        }

        return ResponseEntity.ok(response);
    }
}

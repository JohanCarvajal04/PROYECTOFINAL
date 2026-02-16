package com.app.uteq.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RsaKeyConfig rsaKeyConfig;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    public SecurityConfig(RsaKeyConfig rsaKeyConfig, PasswordEncoder passwordEncoder) {
        this.rsaKeyConfig = rsaKeyConfig;
        this.passwordEncoder = passwordEncoder;
    }

    // ═══════════════════════════════════════════════════════════
    // AUTHENTICATION MANAGER
    // ═══════════════════════════════════════════════════════════
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    // ═══════════════════════════════════════════════════════════
    // SECURITY FILTER CHAIN
    // ═══════════════════════════════════════════════════════════
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF deshabilitado (JWT es stateless, no usa cookies)
                .csrf(csrf -> csrf.disable())
                // Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/2fa/validate").permitAll()
                        .requestMatchers("/api/v1/2fa/validate-backup").permitAll()
                        .anyRequest().authenticated())
                // Sesiones stateless (cada request se autentica via JWT)
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // OAuth2 Resource Server con JWT y converter personalizado
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    // CORS CONFIGURATION (reemplaza CorsConfig.java)
    // ═══════════════════════════════════════════════════════════
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ═══════════════════════════════════════════════════════════
    // JWT AUTHENTICATION CONVERTER
    // Convierte el claim "scope" del JWT a GrantedAuthority
    // de forma que hasRole('ADMIN') y hasAuthority('SCOPE_ROLE_ADMIN')
    // funcionen correctamente.
    // ═══════════════════════════════════════════════════════════

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            String scope = jwt.getClaimAsString("scope");
            if (scope == null || scope.isBlank()) {
                return Collections.emptyList();
            }
            // Cada valor en scope (ej: "ROLE_ADMIN ROLE_STUDENT") se convierte a:
            // - "SCOPE_ROLE_ADMIN" (para hasAuthority('SCOPE_ROLE_ADMIN'))
            // - "ROLE_ADMIN" (para hasRole('ADMIN'))
            return java.util.Arrays.stream(scope.split("\\s+"))
                    .flatMap(s -> {
                        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("SCOPE_" + s));
                        authorities.add(new SimpleGrantedAuthority(s));
                        return authorities.stream();
                    })
                    .collect(Collectors.toList());
        };
    }

    // ═══════════════════════════════════════════════════════════
    // JWT ENCODER y DECODER
    // ═══════════════════════════════════════════════════════════

    /**
     * DECODER: Valida tokens JWT entrantes usando la CLAVE PÚBLICA.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withPublicKey(rsaKeyConfig.publicKey())
                .build();
    }

    /**
     * ENCODER: Genera y firma tokens JWT usando AMBAS claves.
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaKeyConfig.publicKey())
                .privateKey(rsaKeyConfig.privateKey())
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }
}

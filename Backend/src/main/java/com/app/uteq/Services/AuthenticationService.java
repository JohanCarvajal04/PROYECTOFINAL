package com.app.uteq.Services;

import com.app.uteq.Config.JwtService;
import com.app.uteq.Dtos.AuthRequest;
import com.app.uteq.Dtos.AuthResponse;
import com.app.uteq.Dtos.AuthResponse;
import com.app.uteq.Dtos.RegisterRequest;
import com.app.uteq.Dtos.RefreshTokenRequest;
import com.app.uteq.Entity.Roles;
import com.app.uteq.Entity.Users;
import com.app.uteq.Repository.ICredentialsRepository;
import com.app.uteq.Repository.IRefreshTokenRepository;
import com.app.uteq.Repository.IRolesRepository;
import com.app.uteq.Repository.IUsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final IUsersRepository repository;
  private final IRolesRepository rolesRepository;
  private final ICredentialsRepository credentialsRepository;
  private final IRefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // 1. Create Credentials using SP
    Integer credId = credentialsRepository.createCredential(
        passwordEncoder.encode(request.getPassword()),
        LocalDate.now().plusMonths(6)
    );

    // 2. Get Default Role (ROLE_STUDENT from SQL)
    Roles userRole = rolesRepository.findByRoleName("ROLE_STUDENT")
            .orElseThrow(() -> new RuntimeException("Default Role ROLE_STUDENT not found."));

    // 3. Create User using SP
    // Assuming configId = 1 (Default Configuration)
    Integer userId = repository.createUser(
        request.getNames(),
        request.getSurnames(),
        request.getCardId(),
        request.getInstitutionalEmail(),
        request.getPersonalMail(),
        request.getPhoneNumber(),
        1, 
        credId
    );
            
    // 4. Assign Role using SP
    repository.assignRole(userId, userRole.getIdRole());

    // 5. Fetch complete User entity to generate Token
    Users user = repository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User creation failed."));

    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    refreshTokenRepository.createRefreshToken(user.getIdUser(), refreshToken, LocalDateTime.now().plusDays(7), "UNKNOWN");
    return AuthResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .build();
  }

  public AuthResponse authenticate(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    // User is authenticated
    var user = repository.findByInstitutionalEmail(request.getEmail())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    refreshTokenRepository.createRefreshToken(user.getIdUser(), refreshToken, LocalDateTime.now().plusDays(7), "UNKNOWN");
    return AuthResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .build();
  }

  public AuthResponse refreshToken(RefreshTokenRequest request) {
    String token = request.getRefreshToken();
    String userEmail = jwtService.extractUsername(token);
    if (userEmail != null) {
      var user = this.repository.findByInstitutionalEmail(userEmail)
              .orElseThrow();
      if (jwtService.isTokenValid(token, user)) {
        var accessToken = jwtService.generateToken(user);
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(token)
            .build();
      }
    }
    return null;
  }
}

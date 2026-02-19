package com.app.uteq.Controllers;

import com.app.uteq.Dtos.AuthRequest;
import com.app.uteq.Dtos.AuthResponse;
import com.app.uteq.Dtos.RegisterRequest;
import com.app.uteq.Dtos.RefreshTokenRequest;
import com.app.uteq.Services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(service.register(request));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthResponse> authenticate(
      @RequestBody AuthRequest request
  ) {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<AuthResponse> refreshToken(
      @RequestBody RefreshTokenRequest request
  ) {
    AuthResponse response = service.refreshToken(request);
    if (response == null) {
        return ResponseEntity.status(403).build();
    }
    return ResponseEntity.ok(response);
  }
}

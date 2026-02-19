package com.app.uteq.Controllers;

import com.app.uteq.Dtos.UpdateUserRequest;
import com.app.uteq.Dtos.UserResponse;
import com.app.uteq.Dtos.CreateUserRequest;
import com.app.uteq.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> create(@RequestBody CreateUserRequest request) {
        service.createUser(request);
        return ResponseEntity.ok("Usuario creado exitosamente");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(service.listUsers());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> update(@RequestBody UpdateUserRequest request) {
        service.updateUser(request);
        return ResponseEntity.ok("Usuario actualizado con éxito");
    }
}

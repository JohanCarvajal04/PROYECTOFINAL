package com.app.uteq.Controllers;

import com.app.uteq.Entity.TwoFactorAuth;
import com.app.uteq.Services.ITwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/two-factor-auth")
public class TwoFactorAuthController {
    private final ITwoFactorAuthService service;

    @GetMapping
    public ResponseEntity<List<TwoFactorAuth>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TwoFactorAuth> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TwoFactorAuth> create(@RequestBody TwoFactorAuth entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TwoFactorAuth> update(@PathVariable Integer id, @RequestBody TwoFactorAuth entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

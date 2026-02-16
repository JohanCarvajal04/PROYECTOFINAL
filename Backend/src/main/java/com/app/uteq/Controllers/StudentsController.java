package com.app.uteq.Controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.CStudentRequest;
import com.app.uteq.Dtos.StudentResponse;
import com.app.uteq.Dtos.UStudentRequest;
import com.app.uteq.Entity.Students;
import com.app.uteq.Services.IStudentsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
@PreAuthorize("isAuthenticated()")
public class StudentsController {
    
    private final IStudentsService service;

    @GetMapping("/legacy")
    public ResponseEntity<List<Students>> findAllLegacy() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/legacy/{id}")
    public ResponseEntity<Students> findByIdLegacy(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ═════════════════════════════════════════════════════════════
    // NUEVOS ENDPOINTS CON DTOs Y VALIDACIÓN
    // ═════════════════════════════════════════════════════════════

    @GetMapping
    public ResponseEntity<List<StudentResponse>> findAll() {
        return ResponseEntity.ok(service.findAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findStudentById(id));
    }

    @GetMapping("/career/{careerId}")
    public ResponseEntity<List<StudentResponse>> findByCareer(@PathVariable Integer careerId) {
        return ResponseEntity.ok(service.findByCareer(careerId));
    }

    @GetMapping("/semester/{semester}/parallel/{parallel}")
    public ResponseEntity<List<StudentResponse>> findBySemesterAndParallel(
            @PathVariable String semester,
            @PathVariable String parallel) {
        return ResponseEntity.ok(service.findBySemesterAndParallel(semester, parallel));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StudentResponse> findByUserId(@PathVariable Integer userId) {
        return service.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<StudentResponse> enrollStudent(@Valid @RequestBody CStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.enrollStudent(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Integer id,
            @Valid @RequestBody UStudentRequest request) {
        return ResponseEntity.ok(service.updateStudent(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<StudentResponse> changeStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        return ResponseEntity.ok(service.changeStatus(id, status));
    }

    @PostMapping("/{id}/promote")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<StudentResponse> promoteToNextSemester(@PathVariable Integer id) {
        return ResponseEntity.ok(service.promoteToNextSemester(id));
    }

    @PostMapping("/{id}/graduate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR', 'DEAN')")
    public ResponseEntity<StudentResponse> graduate(@PathVariable Integer id) {
        return ResponseEntity.ok(service.graduate(id));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<StudentResponse> withdraw(@PathVariable Integer id) {
        return ResponseEntity.ok(service.withdraw(id));
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR', 'DEAN')")
    public ResponseEntity<StudentResponse> reactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(service.reactivate(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

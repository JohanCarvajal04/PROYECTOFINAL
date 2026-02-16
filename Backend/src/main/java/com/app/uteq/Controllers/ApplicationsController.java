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

import com.app.uteq.Dtos.ApplicationResponse;
import com.app.uteq.Dtos.CApplicationRequest;
import com.app.uteq.Dtos.UApplicationRequest;
import com.app.uteq.Services.IApplicationsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
@PreAuthorize("isAuthenticated()")
public class ApplicationsController {
    private final IApplicationsService service;

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> findAll() {
        return ResponseEntity.ok(service.findAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findApplicationById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationResponse>> findByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(service.findByApplicantUserId(userId));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<ApplicationResponse>> findByPriority(@PathVariable String priority) {
        return ResponseEntity.ok(service.findByPriority(priority));
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody CApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createApplication(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> update(@PathVariable Integer id, 
                                                      @Valid @RequestBody UApplicationRequest request) {
        return ResponseEntity.ok(service.updateApplication(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApplicationResponse> resolve(@PathVariable Integer id,
                                                       @RequestParam String resolution) {
        return ResponseEntity.ok(service.resolveApplication(id, resolution));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponse> reject(@PathVariable Integer id,
                                                      @RequestParam Integer rejectionReasonId) {
        return ResponseEntity.ok(service.rejectApplication(id, rejectionReasonId));
    }
}

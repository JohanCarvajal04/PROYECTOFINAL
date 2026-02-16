package com.app.uteq.Controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.uteq.Dtos.CRequirementRequest;
import com.app.uteq.Dtos.RequirementResponse;
import com.app.uteq.Dtos.URequirementRequest;
import com.app.uteq.Entity.Procedures;
import com.app.uteq.Entity.RequirementsOfTheProcedure;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Services.IRequirementsOfTheProcedureService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/requirements")
@PreAuthorize("isAuthenticated()")
public class RequirementsController {
    private final IRequirementsOfTheProcedureService service;

    @GetMapping
    @PreAuthorize("hasAuthority('REQUISITO_LISTAR')")
    public ResponseEntity<List<RequirementResponse>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REQUISITO_VER')")
    public ResponseEntity<RequirementResponse> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REQUISITO_CREAR')")
    public ResponseEntity<RequirementResponse> create(@Valid @RequestBody CRequirementRequest request) {
        RequirementsOfTheProcedure entity = RequirementsOfTheProcedure.builder()
                .procedure(Procedures.builder().idProcedure(request.getProceduresIdProcedure()).build())
                .requirementName(request.getRequirementName())
                .requirementDescription(request.getRequirementDescription())
                .requirementType(request.getRequirementType())
                .isMandatory(request.getIsMandatory())
                .displayOrder(request.getDisplayOrder())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(service.save(entity)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REQUISITO_MODIFICAR')")
    public ResponseEntity<RequirementResponse> update(@PathVariable Integer id,
            @Valid @RequestBody URequirementRequest request) {
        RequirementsOfTheProcedure entity = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RequirementsOfTheProcedure", "id", id));
        entity.setProcedure(Procedures.builder().idProcedure(request.getProceduresIdProcedure()).build());
        entity.setRequirementName(request.getRequirementName());
        entity.setRequirementDescription(request.getRequirementDescription());
        entity.setRequirementType(request.getRequirementType());
        entity.setIsMandatory(request.getIsMandatory());
        entity.setDisplayOrder(request.getDisplayOrder());
        return ResponseEntity.ok(toResponse(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REQUISITO_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private RequirementResponse toResponse(RequirementsOfTheProcedure entity) {
        return new RequirementResponse(
                entity.getId(),
                entity.getProcedure() != null ? entity.getProcedure().getIdProcedure() : null,
                entity.getRequirementName(),
                entity.getRequirementDescription(),
                entity.getRequirementType(),
                entity.getIsMandatory(),
                entity.getDisplayOrder()
        );
    }
}

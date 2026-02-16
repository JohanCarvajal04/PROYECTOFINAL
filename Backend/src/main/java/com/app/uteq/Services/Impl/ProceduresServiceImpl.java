package com.app.uteq.Services.Impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CProcedureRequest;
import com.app.uteq.Dtos.ProcedureResponse;
import com.app.uteq.Dtos.UProcedureRequest;
import com.app.uteq.Entity.Procedures;
import com.app.uteq.Entity.Workflows;
import com.app.uteq.Exceptions.BadRequestException;
import com.app.uteq.Exceptions.BusinessException;
import com.app.uteq.Exceptions.DuplicateResourceException;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Repository.IApplicationsRepository;
import com.app.uteq.Repository.IProceduresRepository;
import com.app.uteq.Repository.IWorkflowsRepository;
import com.app.uteq.Services.IProceduresService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProceduresServiceImpl implements IProceduresService {

    private final IProceduresRepository repository;
    private final IWorkflowsRepository workflowsRepository;
    private final IApplicationsRepository applicationsRepository;

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Procedures> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Procedures> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Procedures save(Procedures procedure) {
        return repository.save(procedure);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    // ═════════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ProcedureResponse> findAllProcedures() {
        return repository.findByActiveTrue().stream()
                .map(this::toProcedureResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcedureResponse> findAllIncludingInactive() {
        return repository.findAll().stream()
                .map(this::toProcedureResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProcedureResponse findProcedureById(Integer id) {
        Procedures procedure = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite", "id", id));
        return toProcedureResponse(procedure);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcedureResponse> findByProcedureCode(String procedureCode) {
        return repository.findByProcedureCode(procedureCode)
                .map(this::toProcedureResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcedureResponse> findByWorkflow(Integer workflowId) {
        if (!workflowsRepository.existsById(workflowId)) {
            throw new ResourceNotFoundException("Flujo de trabajo", "id", workflowId);
        }
        return repository.findByWorkflowIdWorkflow(workflowId).stream()
                .map(this::toProcedureResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProcedureResponse createProcedure(CProcedureRequest request) {
        // Validar nombre
        if (request.getProcedurename() == null || request.getProcedurename().isBlank()) {
            throw new BadRequestException("El nombre del trámite es requerido");
        }

        // Generar código si no se proporciona
        String code = generateProcedureCode(request.getProcedurename());
        if (repository.existsByProcedureCode(code)) {
            throw new DuplicateResourceException("Trámite", "código", code);
        }

        // Validar workflow
        Workflows workflow = workflowsRepository.findById(request.getWorkflowidworkflow())
                .orElseThrow(() -> new ResourceNotFoundException("Flujo de trabajo", "id", 
                        request.getWorkflowidworkflow()));

        // Validar duración
        if (request.getMaxduration() != null && request.getMaxduration() < 1) {
            throw new BadRequestException("La duración máxima debe ser al menos 1 día");
        }

        Procedures procedure = Procedures.builder()
                .nameProcedure(request.getProcedurename().trim())
                .procedureCode(code)
                .description(request.getDescription())
                .workflow(workflow)
                .estimatedDurationDays(request.getMaxduration())
                .requires2fa(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Procedures saved = repository.save(procedure);
        return toProcedureResponse(saved);
    }

    @Override
    public ProcedureResponse updateProcedure(Integer id, UProcedureRequest request) {
        Procedures procedure = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite", "id", id));

        if (request.getProcedurename() != null && !request.getProcedurename().isBlank()) {
            procedure.setNameProcedure(request.getProcedurename().trim());
        }

        if (request.getDescription() != null) {
            procedure.setDescription(request.getDescription());
        }

        if (request.getMaxduration() != null) {
            if (request.getMaxduration() < 1) {
                throw new BadRequestException("La duración máxima debe ser al menos 1 día");
            }
            procedure.setEstimatedDurationDays(request.getMaxduration());
        }

        if (request.getWorkflowidworkflow() != null && 
            !request.getWorkflowidworkflow().equals(procedure.getWorkflow().getIdWorkflow())) {
            Workflows newWorkflow = workflowsRepository.findById(request.getWorkflowidworkflow())
                    .orElseThrow(() -> new ResourceNotFoundException("Flujo de trabajo", "id", 
                            request.getWorkflowidworkflow()));
            procedure.setWorkflow(newWorkflow);
        }

        Procedures saved = repository.save(procedure);
        return toProcedureResponse(saved);
    }

    @Override
    public ProcedureResponse activateProcedure(Integer id) {
        Procedures procedure = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite", "id", id));

        procedure.setActive(true);
        Procedures saved = repository.save(procedure);
        return toProcedureResponse(saved);
    }

    @Override
    public ProcedureResponse deactivateProcedure(Integer id) {
        Procedures procedure = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite", "id", id));

        procedure.setActive(false);
        Procedures saved = repository.save(procedure);
        return toProcedureResponse(saved);
    }

    @Override
    public void deleteProcedure(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Trámite", "id", id);
        }

        // Verificar si tiene solicitudes asociadas
        boolean hasApplications = !applicationsRepository.findByProcedureIdProcedure(id).isEmpty();
        if (hasApplications) {
            throw new BusinessException("PROCEDURE_HAS_APPLICATIONS", 
                    "No se puede eliminar el trámite porque tiene solicitudes asociadas. " +
                    "Considere desactivarlo en su lugar.");
        }

        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean requires2FA(Integer id) {
        Procedures procedure = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite", "id", id));
        return Boolean.TRUE.equals(procedure.getRequires2fa());
    }

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ═════════════════════════════════════════════════════════════

    private String generateProcedureCode(String name) {
        // Generar código basado en las primeras letras del nombre + timestamp
        String prefix = name.replaceAll("[^A-Za-z]", "")
                .toUpperCase()
                .substring(0, Math.min(name.length(), 4));
        return prefix + "-" + System.currentTimeMillis() % 10000;
    }

    private ProcedureResponse toProcedureResponse(Procedures procedure) {
        return new ProcedureResponse(
                procedure.getIdProcedure(),
                procedure.getNameProcedure(),
                procedure.getDescription(),
                procedure.getEstimatedDurationDays(),
                procedure.getWorkflow() != null ? procedure.getWorkflow().getIdWorkflow() : null,
                procedure.getActive() ? "activo" : "inactivo",
                procedure.getCreatedAt()
        );
    }
}

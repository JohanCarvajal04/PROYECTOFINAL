package com.app.uteq.Services.Impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.ApplicationResponse;
import com.app.uteq.Dtos.CApplicationRequest;
import com.app.uteq.Dtos.UApplicationRequest;
import com.app.uteq.Entity.Applications;
import com.app.uteq.Entity.Procedures;
import com.app.uteq.Entity.RejectionReasons;
import com.app.uteq.Entity.StageTracking;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.BadRequestException;
import com.app.uteq.Exceptions.DuplicateResourceException;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Repository.IApplicationsRepository;
import com.app.uteq.Repository.IProceduresRepository;
import com.app.uteq.Repository.IRejectionReasonsRepository;
import com.app.uteq.Repository.IStageTrackingRepository;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.IApplicationsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationsServiceImpl implements IApplicationsService {

    private final IApplicationsRepository applicationsRepository;
    private final IUsersRepository usersRepository;
    private final IProceduresRepository proceduresRepository;
    private final IStageTrackingRepository stageTrackingRepository;
    private final IRejectionReasonsRepository rejectionReasonsRepository;

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Applications> findAll() {
        return applicationsRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Applications> findById(Integer id) {
        return applicationsRepository.findById(id);
    }

    @Override
    public Applications save(Applications applications) {
        return applicationsRepository.save(applications);
    }

    @Override
    public void deleteById(Integer id) {
        applicationsRepository.deleteById(id);
    }

    // ═══════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAllApplications() {
        return applicationsRepository.findAll().stream()
                .map(this::toApplicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse findApplicationById(Integer id) {
        Applications app = applicationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", id));
        return toApplicationResponse(app);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> findByApplicantUserId(Integer userId) {
        // Verificar que el usuario existe
        if (!usersRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario", "id", userId);
        }
        return applicationsRepository.findByApplicantUserIdUser(userId).stream()
                .map(this::toApplicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> findByPriority(String priority) {
        validatePriority(priority);
        return applicationsRepository.findByPriority(priority).stream()
                .map(this::toApplicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationResponse createApplication(CApplicationRequest request) {
        // Validar código único
        if (applicationsRepository.existsByApplicationCode(request.getApplicationCode())) {
            throw new DuplicateResourceException("Solicitud", "código", request.getApplicationCode());
        }

        // Validar y obtener usuario solicitante
        Users applicant = usersRepository.findById(request.getApplicantUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", 
                        request.getApplicantUserId()));

        // Validar que el usuario esté activo
        if (!applicant.getActive()) {
            throw new BadRequestException("El usuario solicitante no está activo");
        }

        // Validar y obtener procedimiento
        Procedures procedure = proceduresRepository.findById(request.getProceduresIdProcedure())
                .orElseThrow(() -> new ResourceNotFoundException("Procedimiento", "id", 
                        request.getProceduresIdProcedure()));

        // Validar y obtener seguimiento de etapa
        StageTracking stageTracking = stageTrackingRepository.findById(request.getCurrentStageTrackingId())
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento de etapa", "id", 
                        request.getCurrentStageTrackingId()));

        // Crear entidad
        Applications application = Applications.builder()
                .applicationCode(request.getApplicationCode().toUpperCase().trim())
                .creationDate(LocalDateTime.now())
                .estimatedCompletionDate(request.getEstimatedCompletionDate())
                .applicationDetails(request.getApplicationDetails())
                .currentStageTracking(stageTracking)
                .procedure(procedure)
                .applicantUser(applicant)
                .priority(request.getPriority() != null ? request.getPriority() : "normal")
                .build();

        Applications savedApp = applicationsRepository.save(application);
        return toApplicationResponse(savedApp);
    }

    @Override
    public ApplicationResponse updateApplication(Integer id, UApplicationRequest request) {
        Applications existingApp = applicationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", id));

        // Validar código único (si cambió)
        if (!existingApp.getApplicationCode().equalsIgnoreCase(request.getApplicationCode())) {
            if (applicationsRepository.existsByApplicationCode(request.getApplicationCode())) {
                throw new DuplicateResourceException("Solicitud", "código", request.getApplicationCode());
            }
        }

        // Obtener entidades relacionadas
        Users applicant = usersRepository.findById(request.getApplicantUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", 
                        request.getApplicantUserId()));

        Procedures procedure = proceduresRepository.findById(request.getProceduresIdProcedure())
                .orElseThrow(() -> new ResourceNotFoundException("Procedimiento", "id", 
                        request.getProceduresIdProcedure()));

        StageTracking stageTracking = stageTrackingRepository.findById(request.getCurrentStageTrackingId())
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento de etapa", "id", 
                        request.getCurrentStageTrackingId()));

        // Actualizar campos
        existingApp.setApplicationCode(request.getApplicationCode().toUpperCase().trim());
        existingApp.setEstimatedCompletionDate(request.getEstimatedCompletionDate());
        existingApp.setApplicationDetails(request.getApplicationDetails());
        existingApp.setApplicationResolution(request.getApplicationResolution());
        existingApp.setCurrentStageTracking(stageTracking);
        existingApp.setProcedure(procedure);
        existingApp.setApplicantUser(applicant);
        existingApp.setPriority(request.getPriority() != null ? request.getPriority() : "normal");

        // Manejar razón de rechazo si existe
        if (request.getRejectionReasonId() != null) {
            RejectionReasons rejectionReason = rejectionReasonsRepository.findById(request.getRejectionReasonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Razón de rechazo", "id", 
                            request.getRejectionReasonId()));
            existingApp.setRejectionReason(rejectionReason);
        }

        Applications updatedApp = applicationsRepository.save(existingApp);
        return toApplicationResponse(updatedApp);
    }

    @Override
    public void deleteApplication(Integer id) {
        if (!applicationsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Solicitud", "id", id);
        }
        applicationsRepository.deleteById(id);
    }

    @Override
    public ApplicationResponse resolveApplication(Integer id, String resolution) {
        Applications app = applicationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", id));

        if (resolution == null || resolution.isBlank()) {
            throw new BadRequestException("La resolución no puede estar vacía");
        }

        app.setApplicationResolution(resolution);
        app.setActualCompletionDate(LocalDateTime.now());
        
        Applications updatedApp = applicationsRepository.save(app);
        return toApplicationResponse(updatedApp);
    }

    @Override
    public ApplicationResponse rejectApplication(Integer id, Integer rejectionReasonId) {
        Applications app = applicationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", id));

        RejectionReasons rejectionReason = rejectionReasonsRepository.findById(rejectionReasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Razón de rechazo", "id", 
                        rejectionReasonId));

        app.setRejectionReason(rejectionReason);
        app.setActualCompletionDate(LocalDateTime.now());
        
        Applications updatedApp = applicationsRepository.save(app);
        return toApplicationResponse(updatedApp);
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ═══════════════════════════════════════════════════════════

    private void validatePriority(String priority) {
        if (!List.of("baja", "normal", "alta", "urgente").contains(priority.toLowerCase())) {
            throw new BadRequestException("Prioridad inválida. Valores permitidos: baja, normal, alta, urgente");
        }
    }

    private ApplicationResponse toApplicationResponse(Applications app) {
        return new ApplicationResponse(
                app.getId(),
                app.getApplicationCode(),
                app.getCreationDate(),
                app.getEstimatedCompletionDate(),
                app.getActualCompletionDate(),
                app.getApplicationDetails(),
                app.getApplicationResolution(),
                app.getRejectionReason() != null ? app.getRejectionReason().getIdRejectionReason() : null,
                app.getCurrentStageTracking() != null ? app.getCurrentStageTracking().getId() : null,
                app.getProcedure() != null ? app.getProcedure().getIdProcedure() : null,
                app.getApplicantUser() != null ? app.getApplicantUser().getIdUser() : null,
                app.getPriority()
        );
    }
}

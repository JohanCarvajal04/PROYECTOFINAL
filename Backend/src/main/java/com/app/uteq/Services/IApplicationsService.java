package com.app.uteq.Services;

import java.util.List;
import java.util.Optional;

import com.app.uteq.Dtos.ApplicationResponse;
import com.app.uteq.Dtos.CApplicationRequest;
import com.app.uteq.Dtos.UApplicationRequest;
import com.app.uteq.Entity.Applications;

public interface IApplicationsService {

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═══════════════════════════════════════════════════════════
    List<Applications> findAll();
    Optional<Applications> findById(Integer id);
    Applications save(Applications applications);
    void deleteById(Integer id);

    // ═══════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON DTOs Y LÓGICA DE NEGOCIO
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtiene todas las solicitudes como DTOs.
     */
    List<ApplicationResponse> findAllApplications();

    /**
     * Busca una solicitud por ID.
     * @throws ResourceNotFoundException si no existe
     */
    ApplicationResponse findApplicationById(Integer id);

    /**
     * Busca solicitudes por usuario solicitante.
     */
    List<ApplicationResponse> findByApplicantUserId(Integer userId);

    /**
     * Busca solicitudes por prioridad.
     */
    List<ApplicationResponse> findByPriority(String priority);

    /**
     * Crea una nueva solicitud validando reglas de negocio.
     * @throws DuplicateResourceException si el código ya existe
     * @throws ResourceNotFoundException si el usuario o procedimiento no existen
     */
    ApplicationResponse createApplication(CApplicationRequest request);

    /**
     * Actualiza una solicitud existente.
     * @throws ResourceNotFoundException si no existe
     */
    ApplicationResponse updateApplication(Integer id, UApplicationRequest request);

    /**
     * Elimina una solicitud.
     * @throws ResourceNotFoundException si no existe
     */
    void deleteApplication(Integer id);

    /**
     * Resuelve una solicitud con una resolución.
     */
    ApplicationResponse resolveApplication(Integer id, String resolution);

    /**
     * Rechaza una solicitud con una razón.
     */
    ApplicationResponse rejectApplication(Integer id, Integer rejectionReasonId);
}

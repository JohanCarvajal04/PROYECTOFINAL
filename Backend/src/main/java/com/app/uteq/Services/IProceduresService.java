package com.app.uteq.Services;

import java.util.List;
import java.util.Optional;

import com.app.uteq.Dtos.CProcedureRequest;
import com.app.uteq.Dtos.ProcedureResponse;
import com.app.uteq.Dtos.UProcedureRequest;
import com.app.uteq.Entity.Procedures;

public interface IProceduresService {

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═════════════════════════════════════════════════════════════
    List<Procedures> findAll();
    Optional<Procedures> findById(Integer id);
    Procedures save(Procedures procedure);
    void deleteById(Integer id);

    // ═════════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═════════════════════════════════════════════════════════════

    /**
     * Obtiene todos los trámites activos como DTOs.
     */
    List<ProcedureResponse> findAllProcedures();

    /**
     * Obtiene todos los trámites (activos e inactivos).
     */
    List<ProcedureResponse> findAllIncludingInactive();

    /**
     * Busca un trámite por ID.
     * @throws ResourceNotFoundException si no existe
     */
    ProcedureResponse findProcedureById(Integer id);

    /**
     * Busca un trámite por código.
     */
    Optional<ProcedureResponse> findByProcedureCode(String procedureCode);

    /**
     * Busca trámites por flujo de trabajo.
     */
    List<ProcedureResponse> findByWorkflow(Integer workflowId);

    /**
     * Crea un nuevo trámite.
     * @throws DuplicateResourceException si el código ya existe
     * @throws ResourceNotFoundException si el workflow no existe
     */
    ProcedureResponse createProcedure(CProcedureRequest request);

    /**
     * Actualiza un trámite existente.
     */
    ProcedureResponse updateProcedure(Integer id, UProcedureRequest request);

    /**
     * Activa un trámite.
     */
    ProcedureResponse activateProcedure(Integer id);

    /**
     * Desactiva un trámite.
     */
    ProcedureResponse deactivateProcedure(Integer id);

    /**
     * Elimina un trámite (soft delete).
     * @throws BusinessException si tiene solicitudes asociadas
     */
    void deleteProcedure(Integer id);

    /**
     * Verifica si el trámite requiere 2FA.
     */
    boolean requires2FA(Integer id);
}

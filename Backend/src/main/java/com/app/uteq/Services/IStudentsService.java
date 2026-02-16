package com.app.uteq.Services;

import java.util.List;
import java.util.Optional;

import com.app.uteq.Dtos.CStudentRequest;
import com.app.uteq.Dtos.StudentResponse;
import com.app.uteq.Dtos.UStudentRequest;
import com.app.uteq.Entity.Students;

public interface IStudentsService {

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═════════════════════════════════════════════════════════════
    List<Students> findAll();
    Optional<Students> findById(Integer id);
    Students save(Students students);
    void deleteById(Integer id);

    // ═════════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═════════════════════════════════════════════════════════════

    /**
     * Obtiene todos los estudiantes activos como DTOs.
     */
    List<StudentResponse> findAllStudents();

    /**
     * Busca un estudiante por ID.
     * @throws ResourceNotFoundException si no existe
     */
    StudentResponse findStudentById(Integer id);

    /**
     * Busca estudiantes por carrera.
     */
    List<StudentResponse> findByCareer(Integer careerId);

    /**
     * Busca estudiantes por semestre y paralelo.
     */
    List<StudentResponse> findBySemesterAndParallel(String semester, String parallel);

    /**
     * Busca estudiante por ID de usuario.
     */
    Optional<StudentResponse> findByUserId(Integer userId);

    /**
     * Matricula un nuevo estudiante.
     * @throws DuplicateResourceException si el usuario ya está matriculado
     * @throws ResourceNotFoundException si el usuario o carrera no existen
     */
    StudentResponse enrollStudent(CStudentRequest request);

    /**
     * Actualiza datos de un estudiante.
     */
    StudentResponse updateStudent(Integer id, UStudentRequest request);

    /**
     * Cambia el estado de un estudiante.
     */
    StudentResponse changeStatus(Integer id, String newStatus);

    /**
     * Promueve al estudiante al siguiente semestre.
     * @throws BusinessException si no puede ser promovido
     */
    StudentResponse promoteToNextSemester(Integer id);

    /**
     * Grada a un estudiante (cambia estado a 'graduado').
     */
    StudentResponse graduate(Integer id);

    /**
     * Retira a un estudiante.
     */
    StudentResponse withdraw(Integer id);

    /**
     * Reactiva a un estudiante retirado.
     */
    StudentResponse reactivate(Integer id);
}

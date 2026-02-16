package com.app.uteq.Services.Impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CStudentRequest;
import com.app.uteq.Dtos.StudentResponse;
import com.app.uteq.Dtos.UStudentRequest;
import com.app.uteq.Entity.Careers;
import com.app.uteq.Entity.Students;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.BadRequestException;
import com.app.uteq.Exceptions.BusinessException;
import com.app.uteq.Exceptions.DuplicateResourceException;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Repository.ICareersRepository;
import com.app.uteq.Repository.IStudentsRepository;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.IStudentsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentsServiceImpl implements IStudentsService {

    private final IStudentsRepository studentsRepository;
    private final IUsersRepository usersRepository;
    private final ICareersRepository careersRepository;

    // Estados válidos para estudiantes
    private static final List<String> VALID_STATUSES = List.of(
            "activo", "inactivo", "graduado", "retirado", "suspendido"
    );

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Students> findAll() {
        return studentsRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Students> findById(Integer id) {
        return studentsRepository.findById(id);
    }

    @Override
    public Students save(Students students) {
        return studentsRepository.save(students);
    }

    @Override
    public void deleteById(Integer id) {
        studentsRepository.deleteById(id);
    }

    // ═════════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findAllStudents() {
        return studentsRepository.findAll().stream()
                .filter(s -> "activo".equalsIgnoreCase(s.getStatus()))
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findStudentById(Integer id) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));
        return toStudentResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findByCareer(Integer careerId) {
        if (!careersRepository.existsById(careerId)) {
            throw new ResourceNotFoundException("Carrera", "id", careerId);
        }
        return studentsRepository.findByCareerIdCareer(careerId).stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findBySemesterAndParallel(String semester, String parallel) {
        return studentsRepository.findBySemesterAndParallel(semester, parallel.toUpperCase()).stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentResponse> findByUserId(Integer userId) {
        return studentsRepository.findByUserIdUser(userId)
                .map(this::toStudentResponse);
    }

    @Override
    public StudentResponse enrollStudent(CStudentRequest request) {
        // Validar que el usuario existe
        Users user = usersRepository.findById(request.getUsersIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getUsersIdUser()));

        // Validar que el usuario no esté ya matriculado
        if (studentsRepository.findByUserIdUser(request.getUsersIdUser()).isPresent()) {
            throw new DuplicateResourceException("Estudiante", "usuario", request.getUsersIdUser());
        }

        // Validar que la carrera existe
        Careers career = careersRepository.findById(request.getCareersIdCareer())
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", request.getCareersIdCareer()));

        // Validar semestre
        validateSemester(request.getSemester());

        // Validar paralelo
        validateParallel(request.getParallel());

        Students student = Students.builder()
                .semester(request.getSemester())
                .parallel(request.getParallel().toUpperCase())
                .user(user)
                .career(career)
                .enrollmentDate(LocalDate.now())
                .status("activo")
                .build();

        Students saved = studentsRepository.save(student);
        return toStudentResponse(saved);
    }

    @Override
    public StudentResponse updateStudent(Integer id, UStudentRequest request) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));

        // Validar semestre si se proporciona
        if (request.getSemester() != null) {
            validateSemester(request.getSemester());
            student.setSemester(request.getSemester());
        }

        // Validar paralelo si se proporciona
        if (request.getParallel() != null) {
            validateParallel(request.getParallel());
            student.setParallel(request.getParallel().toUpperCase());
        }

        // Validar estado si se proporciona
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            student.setStatus(request.getStatus().toLowerCase());
        }

        // Cambiar carrera si se proporciona
        if (request.getCareersIdCareer() != null && 
            !request.getCareersIdCareer().equals(student.getCareer().getIdCareer())) {
            Careers newCareer = careersRepository.findById(request.getCareersIdCareer())
                    .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", request.getCareersIdCareer()));
            student.setCareer(newCareer);
        }

        Students saved = studentsRepository.save(student);
        return toStudentResponse(saved);
    }

    @Override
    public StudentResponse changeStatus(Integer id, String newStatus) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));

        validateStatus(newStatus);
        student.setStatus(newStatus.toLowerCase());

        Students saved = studentsRepository.save(student);
        return toStudentResponse(saved);
    }

    @Override
    public StudentResponse promoteToNextSemester(Integer id) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));

        if (!"activo".equalsIgnoreCase(student.getStatus())) {
            throw new BusinessException("STUDENT_NOT_ACTIVE", 
                    "Solo estudiantes activos pueden ser promovidos");
        }

        int currentSemester;
        try {
            currentSemester = Integer.parseInt(student.getSemester());
        } catch (NumberFormatException e) {
            throw new BusinessException("INVALID_SEMESTER", 
                    "El semestre actual no es válido para promoción");
        }

        if (currentSemester >= 10) {
            throw new BusinessException("MAX_SEMESTER_REACHED", 
                    "El estudiante ya está en el último semestre. Use 'graduate' para graduarlo.");
        }

        student.setSemester(String.valueOf(currentSemester + 1));
        Students saved = studentsRepository.save(student);
        return toStudentResponse(saved);
    }

    @Override
    public StudentResponse graduate(Integer id) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));

        if ("graduado".equalsIgnoreCase(student.getStatus())) {
            throw new BusinessException("ALREADY_GRADUATED", "El estudiante ya está graduado");
        }

        student.setStatus("graduado");
        Students saved = studentsRepository.save(student);
        return toStudentResponse(saved);
    }

    @Override
    public StudentResponse withdraw(Integer id) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));

        if ("retirado".equalsIgnoreCase(student.getStatus())) {
            throw new BusinessException("ALREADY_WITHDRAWN", "El estudiante ya está retirado");
        }

        if ("graduado".equalsIgnoreCase(student.getStatus())) {
            throw new BusinessException("CANNOT_WITHDRAW_GRADUATED", 
                    "No se puede retirar a un estudiante graduado");
        }

        student.setStatus("retirado");
        Students saved = studentsRepository.save(student);
        return toStudentResponse(saved);
    }

    @Override
    public StudentResponse reactivate(Integer id) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));

        if ("graduado".equalsIgnoreCase(student.getStatus())) {
            throw new BusinessException("CANNOT_REACTIVATE_GRADUATED", 
                    "No se puede reactivar a un estudiante graduado");
        }

        if ("activo".equalsIgnoreCase(student.getStatus())) {
            throw new BusinessException("ALREADY_ACTIVE", "El estudiante ya está activo");
        }

        student.setStatus("activo");
        Students saved = studentsRepository.save(student);
        return toStudentResponse(saved);
    }

    // ═════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ═════════════════════════════════════════════════════════════

    private void validateSemester(String semester) {
        try {
            int sem = Integer.parseInt(semester);
            if (sem < 1 || sem > 10) {
                throw new BadRequestException("El semestre debe estar entre 1 y 10");
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("El semestre debe ser un número válido");
        }
    }

    private void validateParallel(String parallel) {
        if (parallel == null || parallel.length() != 1 || !Character.isLetter(parallel.charAt(0))) {
            throw new BadRequestException("El paralelo debe ser una sola letra (A-Z)");
        }
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status.toLowerCase())) {
            throw new BadRequestException(
                    "Estado inválido. Estados válidos: " + String.join(", ", VALID_STATUSES));
        }
    }

    private StudentResponse toStudentResponse(Students student) {
        Users user = student.getUser();
        Careers career = student.getCareer();
        
        return StudentResponse.builder()
                .idStudent(student.getId())
                .semester(student.getSemester())
                .parallel(student.getParallel())
                .userId(user != null ? user.getIdUser() : null)
                .userName(user != null ? user.getNames() + " " + user.getSurnames() : null)
                .userEmail(user != null ? user.getInstitutionalEmail() : null)
                .careerId(career != null ? career.getIdCareer() : null)
                .careerName(career != null ? career.getCareerName() : null)
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus())
                .build();
    }
}

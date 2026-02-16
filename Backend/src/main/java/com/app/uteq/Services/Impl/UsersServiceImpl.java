package com.app.uteq.Services.Impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CUserRequest;
import com.app.uteq.Dtos.UUserRequest;
import com.app.uteq.Dtos.UserResponse;
import com.app.uteq.Entity.Configurations;
import com.app.uteq.Entity.Users;
import com.app.uteq.Exceptions.DuplicateResourceException;
import com.app.uteq.Exceptions.ResourceNotFoundException;
import com.app.uteq.Repository.IConfigurationsRepository;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.IUsersService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UsersServiceImpl implements IUsersService {

    private final IUsersRepository repository;
    private final IConfigurationsRepository configurationsRepository;

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS LEGACY
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Users> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Users> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Users save(Users user) {
        return repository.save(user);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    // ═══════════════════════════════════════════════════════════
    // NUEVOS MÉTODOS CON LÓGICA DE NEGOCIO
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        return repository.findAll().stream()
                .filter(Users::getActive) // Solo usuarios activos
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findUserById(Integer id) {
        Users user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return toUserResponse(user);
    }

    @Override
    public UserResponse createUser(CUserRequest request) {
        // Validar que el email institucional no exista
        if (repository.findByInstitutionalEmail(request.getInstitutionalEmail()).isPresent()) {
            throw new DuplicateResourceException("Usuario", "email institucional", 
                    request.getInstitutionalEmail());
        }

        // Validar que la cédula no exista
        if (repository.findByCardId(request.getCardId()).isPresent()) {
            throw new DuplicateResourceException("Usuario", "cédula", request.getCardId());
        }

        // Obtener configuración
        Configurations config = configurationsRepository.findById(request.getConfigurationsIdConfiguration())
                .orElseThrow(() -> new ResourceNotFoundException("Configuración", "id", 
                        request.getConfigurationsIdConfiguration()));

        // Crear entidad
        Users user = Users.builder()
                .names(request.getNames().trim())
                .surnames(request.getSurnames().trim())
                .cardId(request.getCardId())
                .institutionalEmail(request.getInstitutionalEmail().toLowerCase().trim())
                .personalMail(request.getPersonalMail() != null ? 
                        request.getPersonalMail().toLowerCase().trim() : null)
                .phoneNumber(request.getPhoneNumber())
                .configuration(config)
                .createdAt(LocalDateTime.now())
                .statement(true)
                .active(true)
                .build();

        Users savedUser = repository.save(user);
        return toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Integer id, UUserRequest request) {
        Users existingUser = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        // Validar email único (si cambió)
        if (!existingUser.getInstitutionalEmail().equalsIgnoreCase(request.getInstitutionalEmail())) {
            repository.findByInstitutionalEmail(request.getInstitutionalEmail())
                    .ifPresent(u -> {
                        throw new DuplicateResourceException("Usuario", "email institucional", 
                                request.getInstitutionalEmail());
                    });
        }

        // Validar cédula única (si cambió)
        if (!existingUser.getCardId().equals(request.getCardId())) {
            repository.findByCardId(request.getCardId())
                    .ifPresent(u -> {
                        throw new DuplicateResourceException("Usuario", "cédula", request.getCardId());
                    });
        }

        // Obtener configuración
        Configurations config = configurationsRepository.findById(request.getConfigurationsIdConfiguration())
                .orElseThrow(() -> new ResourceNotFoundException("Configuración", "id", 
                        request.getConfigurationsIdConfiguration()));

        // Actualizar campos
        existingUser.setNames(request.getNames().trim());
        existingUser.setSurnames(request.getSurnames().trim());
        existingUser.setCardId(request.getCardId());
        existingUser.setInstitutionalEmail(request.getInstitutionalEmail().toLowerCase().trim());
        existingUser.setPersonalMail(request.getPersonalMail() != null ? 
                request.getPersonalMail().toLowerCase().trim() : null);
        existingUser.setPhoneNumber(request.getPhoneNumber());
        existingUser.setStatement(request.getStatement());
        existingUser.setConfiguration(config);
        existingUser.setActive(request.getActive());
        existingUser.setUpdatedAt(LocalDateTime.now());

        Users updatedUser = repository.save(existingUser);
        return toUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(Integer id) {
        Users user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        // Soft delete - solo desactivar
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        repository.save(user);
    }

    @Override
    public UserResponse deactivateUser(Integer id) {
        Users user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        Users savedUser = repository.save(user);
        return toUserResponse(savedUser);
    }

    @Override
    public UserResponse activateUser(Integer id) {
        Users user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        Users savedUser = repository.save(user);
        return toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByInstitutionalEmail(String email) {
        return repository.findByInstitutionalEmail(email)
                .map(this::toUserResponse);
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS DE MAPEO PRIVADOS
    // ═══════════════════════════════════════════════════════════

    private UserResponse toUserResponse(Users user) {
        return new UserResponse(
                user.getIdUser(),
                user.getNames(),
                user.getSurnames(),
                user.getCardId(),
                user.getInstitutionalEmail(),
                user.getPersonalMail(),
                user.getPhoneNumber(),
                user.getStatement(),
                user.getConfiguration() != null ? user.getConfiguration().getId() : null,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getActive()
        );
    }
}

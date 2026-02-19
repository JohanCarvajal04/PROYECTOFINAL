package com.app.uteq.Services;

import com.app.uteq.Dtos.UpdateUserRequest;
import com.app.uteq.Dtos.UserResponse;
import com.app.uteq.Repository.IUsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.app.uteq.Dtos.CreateUserRequest;
import com.app.uteq.Entity.Roles;
import com.app.uteq.Repository.ICredentialsRepository;
import com.app.uteq.Repository.IRolesRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {
    private final IUsersRepository repository;
    private final ICredentialsRepository credentialsRepository;
    private final IRolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(CreateUserRequest request) {
        // 1. Credentials
        Integer credId = credentialsRepository.createCredential(
            passwordEncoder.encode(request.getPassword()),
            LocalDate.now().plusMonths(6)
        );

        // 2. Role (Ensure 'ROLE_' prefix if not present)
        String roleName = request.getRoleName() != null ? request.getRoleName().toUpperCase() : "STUDENT";
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        
        String finalRoleName = roleName;
        Roles role = rolesRepository.findByRoleName(finalRoleName)
            .orElseThrow(() -> new RuntimeException("Role " + finalRoleName + " not found"));

        // 3. User (Default Config ID = 1)
        Integer userId = repository.createUser(
            request.getNames(),
            request.getSurnames(),
            request.getCardId(),
            request.getInstitutionalEmail(),
            request.getPersonalMail(),
            request.getPhoneNumber(),
            1, 
            credId
        );

        // 4. Assign Role
        repository.assignRole(userId, role.getIdRole());
    }

    @Transactional
    public void updateUser(UpdateUserRequest request) {
        repository.updateUser(
            request.getIdUser(),
            request.getNames(),
            request.getSurnames(),
            request.getCardId(),
            request.getInstitutionalEmail(),
            request.getPersonalMail(),
            request.getPhoneNumber()
        );
    }

    public List<UserResponse> listUsers() {
        return repository.listAllUsers().stream()
            .map(obj -> UserResponse.builder()
                .idUser((Integer) obj[0])
                .names((String) obj[1])
                .surnames((String) obj[2])
                .institutionalEmail((String) obj[3])
                .active((Boolean) obj[4])
                .build())
            .collect(Collectors.toList());
    }
}

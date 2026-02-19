package com.app.uteq.Config;
 
import com.app.uteq.Dtos.CreateUserRequest;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Repository.IRolesRepository;
import com.app.uteq.Entity.Roles;
import com.app.uteq.Entity.Users;
import com.app.uteq.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedDataConfig implements CommandLineRunner {

    private final IUsersRepository userRepository;
    private final UserService userService;
    private final IRolesRepository rolesRepository;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "piguave@uteq.edu.ec";

        if (userRepository.findByInstitutionalEmail(adminEmail).isEmpty()) {
            System.out.println("🚀 Creando usuario semilla ADMIN: piguave...");
            
            CreateUserRequest admin = CreateUserRequest.builder()
                    .names("Admin")
                    .surnames("Piguave")
                    .cardId("0999999999")
                    .institutionalEmail(adminEmail)
                    .personalMail("admin.piguave@system.local")
                    .phoneNumber("0999999999")
                    .password("piguave")
                    .roleName("ADMIN")
                    .build();

            try {
                userService.createUser(admin);
                System.out.println("✅ Usuario Semilla 'piguave' CREADO CORRECTAMENTE.");
            } catch (Exception e) {
                System.err.println("❌ Error creando usuario semilla: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️ El usuario semilla 'piguave' ya existe en la base de datos. Verificando roles...");
            Users existingUser = userRepository.findByInstitutionalEmail(adminEmail).get();
            // La relación ManyToMany puede no inicializarse si fetch es LAZY, pero en Users está EAGER
            if (existingUser.getRoles() == null || existingUser.getRoles().isEmpty()) {
                 System.out.println("⚠️ El usuario semilla (ID: " + existingUser.getIdUser() + ") no tiene roles asignados. Asignando rol ADMIN...");
                 try {
                     Roles adminRole = rolesRepository.findByRoleName("ROLE_ADMIN")
                         .orElseThrow(() -> new RuntimeException("Rol ROLE_ADMIN no encontrado"));
                     
                     userRepository.assignRole(existingUser.getIdUser(), adminRole.getIdRole());
                     System.out.println("✅ Rol ADMIN asignado correctamente al usuario semilla existente.");
                 } catch (Exception e) {
                     System.err.println("❌ Error asignando rol al usuario semilla existente: " + e.getMessage());
                     e.printStackTrace();
                 }
            } else {
                 System.out.println("✅ El usuario semilla (ID: " + existingUser.getIdUser() + ") ya tiene roles asignados. Cantidad: " + existingUser.getRoles().size());
                 existingUser.getRoles().forEach(r -> System.out.println("   - Rol detectado: ID=" + r.getIdRole() + ", Nombre=" + r.getRoleName()));
            }
        }
    }
}

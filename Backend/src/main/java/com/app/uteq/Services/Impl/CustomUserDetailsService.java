package com.app.uteq.Services.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.Permissions;
import com.app.uteq.Entity.Roles;
import com.app.uteq.Entity.Users;
import com.app.uteq.Repository.IUsersRepository;

/**
 * UserDetailsService respaldado por JPA.
 * Carga roles como ROLE_XXX y permisos individuales como authorities.
 * El "username" es el institutionalEmail del usuario.
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final IUsersRepository usersRepository;

    public CustomUserDetailsService(IUsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = usersRepository.findByInstitutionalEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));

        if (user.getCredentials() == null) {
            throw new UsernameNotFoundException(
                    "El usuario no tiene credenciales configuradas: " + email);
        }

        // Construir lista de authorities: roles + permisos individuales
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (Roles role : user.getRoles()) {
            // Agregar el rol como authority (ej: ROLE_ADMIN)
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

            // Agregar cada permiso del rol como authority (ej: CAL_CREAR)
            if (role.getPermissions() != null) {
                for (Permissions permission : role.getPermissions()) {
                    SimpleGrantedAuthority permAuthority =
                            new SimpleGrantedAuthority(permission.getCode());
                    if (!authorities.contains(permAuthority)) {
                        authorities.add(permAuthority);
                    }
                }
            }
        }

        return User.builder()
                .username(user.getInstitutionalEmail())
                .password(user.getCredentials().getPasswordHash())
                .authorities(authorities)
                .accountLocked(user.getCredentials().getAccountLocked())
                .disabled(!user.getActive())
                .build();
    }
}

package com.app.uteq.Services.Impl;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.Roles;
import com.app.uteq.Entity.Users;
import com.app.uteq.Repository.IUsersRepository;

/**
 * UserDetailsService respaldado por JPA.
 * Usa la entidad Users existente + Credentials + Roles para autenticación.
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

        // Verificar que tenga credenciales asociadas
        if (user.getCredentials() == null) {
            throw new UsernameNotFoundException(
                    "El usuario no tiene credenciales configuradas: " + email);
        }

        // Mapear roles a authorities de Spring Security
        var authorities = user.getRoles().stream()
                .map(Roles::getRoleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return User.builder()
                .username(user.getInstitutionalEmail())
                .password(user.getCredentials().getPasswordHash())
                .authorities(authorities)
                .accountLocked(user.getCredentials().getAccountLocked())
                .disabled(!user.getActive())
                .build();
    }
}

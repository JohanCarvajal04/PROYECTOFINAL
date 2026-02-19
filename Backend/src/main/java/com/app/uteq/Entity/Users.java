package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer idUser;

    @Column(name = "names", nullable = false, length = 255)
    private String names;

    @Column(name = "surnames", nullable = false, length = 255)
    private String surnames;

    @Column(name = "cardid", nullable = false, length = 10)
    private String cardId;

    @Column(name = "institutionalemail", nullable = false, length = 255)
    private String institutionalEmail;

    @Column(name = "personalmail", length = 255, unique = true)
    private String personalMail;

    @Column(name = "phonenumber", length = 15)
    private String phoneNumber;

    @Column(name = "statement", nullable = false)
    private Boolean statement = true;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "credentialsidcredentials")
    private Credentials credentials;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configurationsidconfiguration")
    private Configurations configuration;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "iduser"),
        inverseJoinColumns = @JoinColumn(name = "idrole")
    )
    private Set<Roles> roles = new HashSet<>();

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // --- UserDetails Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            for (Roles role : roles) {
                String roleName = role.getRoleName();
                if (!roleName.startsWith("ROLE_")) {
                    roleName = "ROLE_" + roleName;
                }
                authorities.add(new SimpleGrantedAuthority(roleName));
                if (role.getPermissions() != null) {
                    for (Permissions p : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(p.getCode()));
                    }
                }
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return (credentials != null) ? credentials.getPasswordHash() : null;
    }

    @Override
    public String getUsername() {
        return institutionalEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return (credentials != null) ? !credentials.getAccountLocked() : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}

package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrole")
    private Integer idRole;

    @Column(name = "rolename", unique = true, nullable = false, length = 100)
    private String roleName;

    @Column(name = "roledescription", columnDefinition = "TEXT")
    private String roleDescription;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "idrole"), inverseJoinColumns = @JoinColumn(name = "idpermission"))
    private Set<Permissions> permissions;
}

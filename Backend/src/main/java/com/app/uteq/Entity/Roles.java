package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrole")
    private Integer idRole;

    @Column(name = "rolename", nullable = false, length = 100, unique = true)
    private String roleName;

    @Column(name = "roledescription")
    private String roleDescription;
}


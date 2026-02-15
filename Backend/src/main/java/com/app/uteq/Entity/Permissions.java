package com.app.uteq.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "permissions")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Permissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpermission")
    private Integer idPermission;

    @Column(name = "code", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "description")
    private String description;
}

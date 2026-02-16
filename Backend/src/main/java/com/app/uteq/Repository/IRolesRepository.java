package com.app.uteq.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.uteq.Entity.Roles;

public interface IRolesRepository extends JpaRepository<Roles, Integer> {

    Optional<Roles> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}

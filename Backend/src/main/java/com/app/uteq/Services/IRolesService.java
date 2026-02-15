package com.app.uteq.Services;

import com.app.uteq.Entity.Roles;

import java.util.List;
import java.util.Optional;

public interface IRolesService {
    List<Roles> findAll();

    Optional<Roles> findById(Integer id);

    Roles save(Roles role);

    void deleteById(Integer id);
}

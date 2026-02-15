package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.Roles;
import com.app.uteq.Repository.IRolesRepository;
import com.app.uteq.Services.IRolesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolesServiceImpl implements IRolesService {
    private final IRolesRepository repository;

    @Override
    public List<Roles> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Roles> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Roles save(Roles role) {
        return repository.save(role);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}

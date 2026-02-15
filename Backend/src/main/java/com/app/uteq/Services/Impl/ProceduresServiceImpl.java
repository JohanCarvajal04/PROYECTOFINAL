package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.Procedures;
import com.app.uteq.Repository.IProceduresRepository;
import com.app.uteq.Services.IProceduresService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProceduresServiceImpl implements IProceduresService {
    private final IProceduresRepository repository;

    @Override
    public List<Procedures> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Procedures> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Procedures save(Procedures procedure) {
        return repository.save(procedure);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}

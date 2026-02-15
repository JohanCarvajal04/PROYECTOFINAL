package com.app.uteq.Services;

import com.app.uteq.Entity.Procedures;

import java.util.List;
import java.util.Optional;

public interface IProceduresService {
    List<Procedures> findAll();

    Optional<Procedures> findById(Integer id);

    Procedures save(Procedures procedure);

    void deleteById(Integer id);
}

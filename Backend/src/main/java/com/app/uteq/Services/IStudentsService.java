package com.app.uteq.Services;

import com.app.uteq.Entity.Students;
import java.util.List;
import java.util.Optional;

public interface IStudentsService {
    List<Students> findAll();

    Optional<Students> findById(Integer id);

    Students save(Students students);

    void deleteById(Integer id);
}

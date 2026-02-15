package com.app.uteq.Services;

import com.app.uteq.Entity.Applications;
import java.util.List;
import java.util.Optional;

public interface IApplicationsService {
    List<Applications> findAll();

    Optional<Applications> findById(Integer id);

    Applications save(Applications applications);

    void deleteById(Integer id);
}

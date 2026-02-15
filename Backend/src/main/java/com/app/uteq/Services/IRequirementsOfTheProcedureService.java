package com.app.uteq.Services;

import com.app.uteq.Entity.RequirementsOfTheProcedure;
import java.util.List;
import java.util.Optional;

public interface IRequirementsOfTheProcedureService {
    List<RequirementsOfTheProcedure> findAll();

    Optional<RequirementsOfTheProcedure> findById(Integer id);

    RequirementsOfTheProcedure save(RequirementsOfTheProcedure requirementsOfTheProcedure);

    void deleteById(Integer id);
}

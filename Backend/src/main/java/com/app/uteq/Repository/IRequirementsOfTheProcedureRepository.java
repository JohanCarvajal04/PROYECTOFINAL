package com.app.uteq.Repository;

import com.app.uteq.Entity.RequirementsOfTheProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRequirementsOfTheProcedureRepository extends JpaRepository<RequirementsOfTheProcedure, Integer> {
}

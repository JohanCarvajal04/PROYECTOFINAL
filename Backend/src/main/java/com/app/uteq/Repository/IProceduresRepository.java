package com.app.uteq.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.uteq.Entity.Procedures;

public interface IProceduresRepository extends JpaRepository<Procedures, Integer> {

    Optional<Procedures> findByProcedureCode(String procedureCode);

    List<Procedures> findByActiveTrue();

    List<Procedures> findByActiveFalse();

    List<Procedures> findByWorkflowIdWorkflow(Integer workflowId);

    boolean existsByProcedureCode(String procedureCode);

    boolean existsByNameProcedure(String nameProcedure);
}

package com.app.uteq.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.uteq.Entity.Applications;

@Repository
public interface IApplicationsRepository extends JpaRepository<Applications, Integer> {

    Optional<Applications> findByApplicationCode(String applicationCode);

    List<Applications> findByApplicantUserIdUser(Integer userId);

    List<Applications> findByPriority(String priority);

    List<Applications> findByProcedureIdProcedure(Integer procedureId);

    boolean existsByApplicationCode(String applicationCode);
}

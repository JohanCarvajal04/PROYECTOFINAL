package com.app.uteq.Repository;

import com.app.uteq.Entity.Applications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IApplicationsRepository extends JpaRepository<Applications, Integer> {
}

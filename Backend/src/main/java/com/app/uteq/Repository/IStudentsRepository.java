package com.app.uteq.Repository;

import com.app.uteq.Entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IStudentsRepository extends JpaRepository<Students, Integer> {
}

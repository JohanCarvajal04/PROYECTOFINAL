package com.app.uteq.Repository;

import com.app.uteq.Entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IStudentsRepository extends JpaRepository<Students, Integer> {

    Optional<Students> findByUserIdUser(Integer userId);

    List<Students> findByCareerIdCareer(Integer careerId);

    List<Students> findBySemesterAndParallel(String semester, String parallel);

    List<Students> findByStatus(String status);

    boolean existsByUserIdUser(Integer userId);
}

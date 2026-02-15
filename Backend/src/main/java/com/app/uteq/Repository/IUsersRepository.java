package com.app.uteq.Repository;

import com.app.uteq.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByInstitutionalEmail(String institutionalEmail);
}

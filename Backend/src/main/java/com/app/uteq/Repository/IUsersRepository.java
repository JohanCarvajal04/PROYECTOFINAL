package com.app.uteq.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.uteq.Entity.Users;

public interface IUsersRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByInstitutionalEmail(String institutionalEmail);

    Optional<Users> findByCardId(String cardId);

    Optional<Users> findByPersonalMail(String personalMail);

    List<Users> findByActiveTrue();

    List<Users> findByActiveFalse();

    boolean existsByInstitutionalEmail(String institutionalEmail);

    boolean existsByCardId(String cardId);
}

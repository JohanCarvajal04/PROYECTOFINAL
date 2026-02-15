package com.app.uteq.Repository;

import com.app.uteq.Entity.SessionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISessionTokenRepository extends JpaRepository<SessionToken, Integer> {
}

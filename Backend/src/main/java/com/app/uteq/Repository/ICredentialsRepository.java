package com.app.uteq.Repository;

import com.app.uteq.Entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICredentialsRepository extends JpaRepository<Credentials, Integer> {
}

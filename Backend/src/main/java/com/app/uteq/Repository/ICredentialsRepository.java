package com.app.uteq.Repository;

import com.app.uteq.Entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

@Repository
public interface ICredentialsRepository extends JpaRepository<Credentials, Integer> {
    @Procedure(procedureName = "public.spi_credential")
    Integer createCredential(@Param("p_passwordhash") String p_passwordhash, @Param("p_expirydate") LocalDate p_expirydate);
}

package com.app.uteq.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.uteq.Entity.TwoFactorAuth;

@Repository
public interface ITwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Integer> {

    Optional<TwoFactorAuth> findByCredentials_Id(Integer credentialsId);

    boolean existsByCredentials_IdAndEnabledTrue(Integer credentialsId);
}

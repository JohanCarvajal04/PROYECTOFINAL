package com.app.uteq.Repository;

import com.app.uteq.Entity.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Integer> {
}

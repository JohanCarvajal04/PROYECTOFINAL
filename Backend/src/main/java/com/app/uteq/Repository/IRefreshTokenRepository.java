package com.app.uteq.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.RefreshToken;
import com.app.uteq.Entity.Users;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevokedFalse(Users user);

   @Modifying
@Transactional
@Query(value = "CALL spu_revoke_all_refresh_tokens(:userId)", nativeQuery = true)
void revokeAllByUser(@Param("userId") Integer userId);
}

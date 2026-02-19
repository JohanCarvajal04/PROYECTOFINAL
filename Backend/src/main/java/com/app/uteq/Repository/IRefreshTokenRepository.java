package com.app.uteq.Repository;

import com.app.uteq.Entity.RefreshToken;
import com.app.uteq.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(Users user);

    @Procedure(procedureName = "public.spi_refresh_token")
    void createRefreshToken(
        @Param("p_userid") Integer p_userid, 
        @Param("p_token") String p_token, 
        @Param("p_expires") LocalDateTime p_expires, 
        @Param("p_device") String p_device
    );

    @Procedure(procedureName = "public.spu_revoke_token")
    void revokeToken(@Param("p_token") String p_token);
}

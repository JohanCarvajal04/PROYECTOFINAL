package com.app.uteq.Services;

import com.app.uteq.Entity.RefreshToken;
import java.util.List;
import java.util.Optional;

public interface IRefreshTokenService {
    List<RefreshToken> findAll();

    Optional<RefreshToken> findById(Long id);

    RefreshToken save(RefreshToken refreshToken);

    void deleteById(Long id);
}

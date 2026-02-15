package com.app.uteq.Services;

import com.app.uteq.Entity.TwoFactorAuth;
import java.util.List;
import java.util.Optional;

public interface ITwoFactorAuthService {
    List<TwoFactorAuth> findAll();

    Optional<TwoFactorAuth> findById(Integer id);

    TwoFactorAuth save(TwoFactorAuth twoFactorAuth);

    void deleteById(Integer id);
}

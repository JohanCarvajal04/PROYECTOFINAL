package com.app.uteq.Services;

import java.util.List;
import java.util.Optional;

import com.app.uteq.Dtos.TwoFactorAuthResponse;
import com.app.uteq.Dtos.TwoFactorSetupResponse;
import com.app.uteq.Entity.TwoFactorAuth;

public interface ITwoFactorAuthService {

    // ─── CRUD base ─────────────────────────────────────────
    List<TwoFactorAuth> findAll();
    Optional<TwoFactorAuth> findById(Integer id);
    TwoFactorAuth save(TwoFactorAuth twoFactorAuth);
    void deleteById(Integer id);

    // ─── 2FA Operations ────────────────────────────────────
    TwoFactorSetupResponse setup2FA(String email);
    boolean verifyAndEnable2FA(String email, int code);
    void disable2FA(String email, int code);
    boolean validateCode(String email, int code);
    boolean validateBackupCode(String email, String backupCode);
    boolean is2FAEnabled(String email);
    TwoFactorAuthResponse getStatus(String email);
    List<String> regenerateBackupCodes(String email, int code);
}

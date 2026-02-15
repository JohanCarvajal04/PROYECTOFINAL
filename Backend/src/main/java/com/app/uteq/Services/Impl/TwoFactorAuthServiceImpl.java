package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.TwoFactorAuth;
import com.app.uteq.Repository.ITwoFactorAuthRepository;
import com.app.uteq.Services.ITwoFactorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TwoFactorAuthServiceImpl implements ITwoFactorAuthService {

    @Autowired
    private ITwoFactorAuthRepository twoFactorAuthRepository;

    @Override
    public List<TwoFactorAuth> findAll() {
        return twoFactorAuthRepository.findAll();
    }

    @Override
    public Optional<TwoFactorAuth> findById(Integer id) {
        return twoFactorAuthRepository.findById(id);
    }

    @Override
    public TwoFactorAuth save(TwoFactorAuth twoFactorAuth) {
        return twoFactorAuthRepository.save(twoFactorAuth);
    }

    @Override
    public void deleteById(Integer id) {
        twoFactorAuthRepository.deleteById(id);
    }
}

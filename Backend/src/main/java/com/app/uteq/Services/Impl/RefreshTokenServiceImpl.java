package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.RefreshToken;
import com.app.uteq.Repository.IRefreshTokenRepository;
import com.app.uteq.Services.IRefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements IRefreshTokenService {
    private final IRefreshTokenRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return repository.save(refreshToken);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

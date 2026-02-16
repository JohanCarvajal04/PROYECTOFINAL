package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.SessionToken;
import com.app.uteq.Repository.ISessionTokenRepository;
import com.app.uteq.Services.ISessionTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionTokenServiceImpl implements ISessionTokenService {

    private final ISessionTokenRepository sessionTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SessionToken> findAll() {
        return sessionTokenRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionToken> findById(Integer id) {
        return sessionTokenRepository.findById(id);
    }

    @Override
    public SessionToken save(SessionToken sessionToken) {
        return sessionTokenRepository.save(sessionToken);
    }

    @Override
    public void deleteById(Integer id) {
        sessionTokenRepository.deleteById(id);
    }
}

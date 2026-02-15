package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.SessionToken;
import com.app.uteq.Repository.ISessionTokenRepository;
import com.app.uteq.Services.ISessionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionTokenServiceImpl implements ISessionTokenService {

    @Autowired
    private ISessionTokenRepository sessionTokenRepository;

    @Override
    public List<SessionToken> findAll() {
        return sessionTokenRepository.findAll();
    }

    @Override
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

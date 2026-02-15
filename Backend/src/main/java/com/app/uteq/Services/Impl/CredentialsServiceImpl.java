package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.Credentials;
import com.app.uteq.Repository.ICredentialsRepository;
import com.app.uteq.Services.ICredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CredentialsServiceImpl implements ICredentialsService {

    @Autowired
    private ICredentialsRepository credentialsRepository;

    @Override
    public List<Credentials> findAll() {
        return credentialsRepository.findAll();
    }

    @Override
    public Optional<Credentials> findById(Integer id) {
        return credentialsRepository.findById(id);
    }

    @Override
    public Credentials save(Credentials credentials) {
        return credentialsRepository.save(credentials);
    }

    @Override
    public void deleteById(Integer id) {
        credentialsRepository.deleteById(id);
    }
}

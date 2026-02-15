package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.DigitalSignatures;
import com.app.uteq.Repository.IDigitalSignaturesRepository;
import com.app.uteq.Services.IDigitalSignaturesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DigitalSignaturesServiceImpl implements IDigitalSignaturesService {

    @Autowired
    private IDigitalSignaturesRepository digitalSignaturesRepository;

    @Override
    public List<DigitalSignatures> findAll() {
        return digitalSignaturesRepository.findAll();
    }

    @Override
    public Optional<DigitalSignatures> findById(Integer id) {
        return digitalSignaturesRepository.findById(id);
    }

    @Override
    public DigitalSignatures save(DigitalSignatures digitalSignatures) {
        return digitalSignaturesRepository.save(digitalSignatures);
    }

    @Override
    public void deleteById(Integer id) {
        digitalSignaturesRepository.deleteById(id);
    }
}

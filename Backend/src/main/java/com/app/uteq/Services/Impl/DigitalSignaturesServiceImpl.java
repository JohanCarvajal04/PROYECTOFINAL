package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.DigitalSignatures;
import com.app.uteq.Repository.IDigitalSignaturesRepository;
import com.app.uteq.Services.IDigitalSignaturesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DigitalSignaturesServiceImpl implements IDigitalSignaturesService {

    private final IDigitalSignaturesRepository digitalSignaturesRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DigitalSignatures> findAll() {
        return digitalSignaturesRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
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

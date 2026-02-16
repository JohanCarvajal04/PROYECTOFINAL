package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.RequirementsOfTheProcedure;
import com.app.uteq.Repository.IRequirementsOfTheProcedureRepository;
import com.app.uteq.Services.IRequirementsOfTheProcedureService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RequirementsOfTheProcedureServiceImpl implements IRequirementsOfTheProcedureService {

    private final IRequirementsOfTheProcedureRepository requirementsOfTheProcedureRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RequirementsOfTheProcedure> findAll() {
        return requirementsOfTheProcedureRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RequirementsOfTheProcedure> findById(Integer id) {
        return requirementsOfTheProcedureRepository.findById(id);
    }

    @Override
    public RequirementsOfTheProcedure save(RequirementsOfTheProcedure requirementsOfTheProcedure) {
        return requirementsOfTheProcedureRepository.save(requirementsOfTheProcedure);
    }

    @Override
    public void deleteById(Integer id) {
        requirementsOfTheProcedureRepository.deleteById(id);
    }
}

package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.RequirementsOfTheProcedure;
import com.app.uteq.Repository.IRequirementsOfTheProcedureRepository;
import com.app.uteq.Services.IRequirementsOfTheProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RequirementsOfTheProcedureServiceImpl implements IRequirementsOfTheProcedureService {

    @Autowired
    private IRequirementsOfTheProcedureRepository requirementsOfTheProcedureRepository;

    @Override
    public List<RequirementsOfTheProcedure> findAll() {
        return requirementsOfTheProcedureRepository.findAll();
    }

    @Override
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

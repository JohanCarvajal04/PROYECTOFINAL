package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.Applications;
import com.app.uteq.Repository.IApplicationsRepository;
import com.app.uteq.Services.IApplicationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationsServiceImpl implements IApplicationsService {

    @Autowired
    private IApplicationsRepository applicationsRepository;

    @Override
    public List<Applications> findAll() {
        return applicationsRepository.findAll();
    }

    @Override
    public Optional<Applications> findById(Integer id) {
        return applicationsRepository.findById(id);
    }

    @Override
    public Applications save(Applications applications) {
        return applicationsRepository.save(applications);
    }

    @Override
    public void deleteById(Integer id) {
        applicationsRepository.deleteById(id);
    }
}

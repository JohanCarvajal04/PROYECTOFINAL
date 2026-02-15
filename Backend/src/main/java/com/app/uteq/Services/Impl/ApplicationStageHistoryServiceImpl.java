package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.ApplicationStageHistory;
import com.app.uteq.Repository.IApplicationStageHistoryRepository;
import com.app.uteq.Services.IApplicationStageHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationStageHistoryServiceImpl implements IApplicationStageHistoryService {

    @Autowired
    private IApplicationStageHistoryRepository applicationStageHistoryRepository;

    @Override
    public List<ApplicationStageHistory> findAll() {
        return applicationStageHistoryRepository.findAll();
    }

    @Override
    public Optional<ApplicationStageHistory> findById(Integer id) {
        return applicationStageHistoryRepository.findById(id);
    }

    @Override
    public ApplicationStageHistory save(ApplicationStageHistory applicationStageHistory) {
        return applicationStageHistoryRepository.save(applicationStageHistory);
    }

    @Override
    public void deleteById(Integer id) {
        applicationStageHistoryRepository.deleteById(id);
    }
}

package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.ApplicationStageHistory;
import com.app.uteq.Repository.IApplicationStageHistoryRepository;
import com.app.uteq.Services.IApplicationStageHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationStageHistoryServiceImpl implements IApplicationStageHistoryService {

    private final IApplicationStageHistoryRepository applicationStageHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationStageHistory> findAll() {
        return applicationStageHistoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
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

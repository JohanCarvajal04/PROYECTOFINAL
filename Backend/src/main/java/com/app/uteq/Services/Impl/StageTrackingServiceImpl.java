package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.StageTracking;
import com.app.uteq.Repository.IStageTrackingRepository;
import com.app.uteq.Services.IStageTrackingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StageTrackingServiceImpl implements IStageTrackingService {

    private final IStageTrackingRepository stageTrackingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StageTracking> findAll() {
        return stageTrackingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StageTracking> findById(Integer id) {
        return stageTrackingRepository.findById(id);
    }

    @Override
    public StageTracking save(StageTracking stageTracking) {
        return stageTrackingRepository.save(stageTracking);
    }

    @Override
    public void deleteById(Integer id) {
        stageTrackingRepository.deleteById(id);
    }
}

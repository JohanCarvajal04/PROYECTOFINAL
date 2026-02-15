package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.StageTracking;
import com.app.uteq.Repository.IStageTrackingRepository;
import com.app.uteq.Services.IStageTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StageTrackingServiceImpl implements IStageTrackingService {

    @Autowired
    private IStageTrackingRepository stageTrackingRepository;

    @Override
    public List<StageTracking> findAll() {
        return stageTrackingRepository.findAll();
    }

    @Override
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

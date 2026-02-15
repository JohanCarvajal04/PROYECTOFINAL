package com.app.uteq.Services;

import com.app.uteq.Entity.StageTracking;
import java.util.List;
import java.util.Optional;

public interface IStageTrackingService {
    List<StageTracking> findAll();

    Optional<StageTracking> findById(Integer id);

    StageTracking save(StageTracking stageTracking);

    void deleteById(Integer id);
}

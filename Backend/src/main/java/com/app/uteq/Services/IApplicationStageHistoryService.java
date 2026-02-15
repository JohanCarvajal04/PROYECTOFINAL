package com.app.uteq.Services;

import com.app.uteq.Entity.ApplicationStageHistory;
import java.util.List;
import java.util.Optional;

public interface IApplicationStageHistoryService {
    List<ApplicationStageHistory> findAll();

    Optional<ApplicationStageHistory> findById(Integer id);

    ApplicationStageHistory save(ApplicationStageHistory applicationStageHistory);

    void deleteById(Integer id);
}

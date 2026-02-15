package com.app.uteq.Repository;

import com.app.uteq.Entity.ApplicationStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IApplicationStageHistoryRepository extends JpaRepository<ApplicationStageHistory, Integer> {
}

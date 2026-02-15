package com.app.uteq.Repository;

import com.app.uteq.Entity.StageTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IStageTrackingRepository extends JpaRepository<StageTracking, Integer> {
}

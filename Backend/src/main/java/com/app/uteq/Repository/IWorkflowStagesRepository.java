package com.app.uteq.Repository;

import com.app.uteq.Entity.WorkflowStages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWorkflowStagesRepository extends JpaRepository<WorkflowStages, Integer> {
}

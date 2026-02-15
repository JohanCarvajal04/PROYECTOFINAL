package com.app.uteq.Services;

import com.app.uteq.Entity.WorkflowStages;

import java.util.List;
import java.util.Optional;

public interface IWorkflowStagesService {
    List<WorkflowStages> findAll();

    Optional<WorkflowStages> findById(Integer id);

    WorkflowStages save(WorkflowStages workflowStage);

    void deleteById(Integer id);
}

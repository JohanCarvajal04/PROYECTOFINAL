package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.WorkflowStages;
import com.app.uteq.Repository.IWorkflowStagesRepository;
import com.app.uteq.Services.IWorkflowStagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowStagesServiceImpl implements IWorkflowStagesService {
    private final IWorkflowStagesRepository repository;

    @Override
    public List<WorkflowStages> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<WorkflowStages> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public WorkflowStages save(WorkflowStages workflowStage) {
        return repository.save(workflowStage);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}

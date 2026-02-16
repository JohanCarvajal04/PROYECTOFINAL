package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.WorkflowStages;
import com.app.uteq.Repository.IWorkflowStagesRepository;
import com.app.uteq.Services.IWorkflowStagesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowStagesServiceImpl implements IWorkflowStagesService {
    private final IWorkflowStagesRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowStages> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
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

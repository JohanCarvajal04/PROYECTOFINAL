package com.app.uteq.Services;

import com.app.uteq.Dtos.CWorkflowRequest;
import com.app.uteq.Dtos.UWorkflowRequest;
import com.app.uteq.Dtos.WorkflowResponse;

import java.util.List;

public interface IWorkflowsService {
    void createWorkflow(CWorkflowRequest request);
    void updateWorkflow(UWorkflowRequest request);
    void deleteWorkflow(Integer idworkflow);
    List<WorkflowResponse> listWorkflow(Boolean onlyActive);
}

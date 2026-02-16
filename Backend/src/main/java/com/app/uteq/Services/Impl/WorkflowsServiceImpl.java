package com.app.uteq.Services.Impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CWorkflowRequest;
import com.app.uteq.Dtos.UWorkflowRequest;
import com.app.uteq.Dtos.WorkflowResponse;
import com.app.uteq.Repository.IWorkflowsRepository;
import com.app.uteq.Services.IWorkflowsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowsServiceImpl implements IWorkflowsService {
    private final IWorkflowsRepository repository;

    @Override
    public void createWorkflow(CWorkflowRequest request) {
        repository.spiWorkflow(
                request.getWorkflowname(),
                request.getWorkflowdescription(),
                request.getActive()
        );
    }

    @Override
    public void updateWorkflow(UWorkflowRequest request) {
        repository.spuWorkflow(
                request.getIdworkflow(),
                request.getWorkflowname(),
                request.getWorkflowdescription(),
                request.getActive()
        );
    }

    @Override
    public void deleteWorkflow(Integer idworkflow) {
        repository.spdWorkflow(idworkflow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowResponse> listWorkflow(Boolean onlyActive) {
        List<Object[]> rows = repository.fnListWorkflows(onlyActive);

        return rows.stream().map(r -> new WorkflowResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2]),
                toLocalDateTime(r[3]),
                toBool(r[4])
        )).toList();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        throw new IllegalArgumentException("Tipo numérico no soportado: " + v.getClass());
    }

    private String toStr(Object v) {
        return v == null ? null : v.toString();
    }

    private Boolean toBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(v.toString());
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        if (v instanceof java.util.Date d) return new Timestamp(d.getTime()).toLocalDateTime();
        throw new IllegalArgumentException("Tipo timestamp no soportado: " + v.getClass());
    }
}
